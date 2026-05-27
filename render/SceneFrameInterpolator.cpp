#include "render/SceneFrameInterpolator.h"

#include <algorithm>
#include <cmath>
#include <unordered_set>

namespace render {

namespace {

float dot(const Quat& a, const Quat& b) {
  return a.w * b.w + a.x * b.x + a.y * b.y + a.z * b.z;
}

Quat normalize(Quat q) {
  const float n = std::sqrt(dot(q, q));
  if (n <= 1e-6F) {
    return Quat{};
  }
  q.w /= n;
  q.x /= n;
  q.y /= n;
  q.z /= n;
  return q;
}

float clamp01(float v) {
  return std::max(0.0F, std::min(1.0F, v));
}

}  // namespace

Vec3 Vec3::operator+(const Vec3& rhs) const { return {x + rhs.x, y + rhs.y, z + rhs.z}; }
Vec3 Vec3::operator-(const Vec3& rhs) const { return {x - rhs.x, y - rhs.y, z - rhs.z}; }
Vec3 Vec3::operator*(float s) const { return {x * s, y * s, z * s}; }

SceneFrameInterpolator::SceneFrameInterpolator(std::size_t maxFrames) : maxFrames_(maxFrames) {}

void SceneFrameInterpolator::pushInputFrame(const DrivingSceneFrame& frame) {
  if (!queue_.empty() && frame.timestampMs < queue_.back().timestampMs) {
    return;
  }
  queue_.push_back(frame);
  while (queue_.size() > maxFrames_) {
    queue_.pop_front();
  }
  if (renderClockMs_ == 0) {
    renderClockMs_ = frame.timestampMs;
  }
}

void SceneFrameInterpolator::tickRenderClock() {
  const int64_t dtMs = static_cast<int64_t>(std::llround(1000.0 / std::max(1.0F, renderHz_)));
  renderClockMs_ += dtMs;
}

void SceneFrameInterpolator::setRenderHz(float hz) { renderHz_ = std::max(1.0F, hz); }
void SceneFrameInterpolator::setExtrapolationThresholdMs(int64_t thresholdMs) { extrapolationThresholdMs_ = thresholdMs; }
void SceneFrameInterpolator::setMaxExtrapolationMs(int64_t maxMs) { maxExtrapolationMs_ = std::min<int64_t>(150, std::max<int64_t>(0, maxMs)); }
void SceneFrameInterpolator::setHudLowPassAlpha(float alpha) { hudAlpha_ = clamp01(alpha); }

SceneFrameInterpolator::BracketFrames SceneFrameInterpolator::findBracket() const {
  BracketFrames out;
  if (queue_.empty()) {
    return out;
  }
  if (queue_.size() == 1) {
    out.f0 = &queue_.back();
    out.f1 = &queue_.back();
    return out;
  }

  for (std::size_t i = 1; i < queue_.size(); ++i) {
    const auto& prev = queue_[i - 1];
    const auto& next = queue_[i];
    if (prev.timestampMs <= renderClockMs_ && renderClockMs_ <= next.timestampMs) {
      out.f0 = &prev;
      out.f1 = &next;
      const auto den = static_cast<float>(next.timestampMs - prev.timestampMs);
      out.t = den > 0.0F ? static_cast<float>(renderClockMs_ - prev.timestampMs) / den : 0.0F;
      return out;
    }
  }

  out.f0 = &queue_[queue_.size() - 1];
  out.f1 = out.f0;
  const int64_t lagMs = renderClockMs_ - out.f0->timestampMs;
  if (lagMs > extrapolationThresholdMs_) {
    out.extrapolated = true;
    out.degraded = lagMs > maxExtrapolationMs_;
  }
  return out;
}

RenderSceneFrame SceneFrameInterpolator::renderFrame() {
  RenderSceneFrame out;
  out.timestampMs = renderClockMs_;
  const BracketFrames bracket = findBracket();
  if (!bracket.f0) return out;

  const DrivingSceneFrame& base = *bracket.f0;
  std::unordered_map<std::string, const VehicleState*> bmap;
  if (bracket.f1) {
    for (const auto& v : bracket.f1->vehicles) bmap[v.id] = &v;
  }

  std::unordered_set<std::string> currentIds;
  for (const auto& va : base.vehicles) {
    currentIds.insert(va.id);
    VehicleState r = va;
    if (bracket.extrapolated) {
      const int64_t lagMs = std::min<int64_t>(maxExtrapolationMs_, std::max<int64_t>(0, renderClockMs_ - base.timestampMs));
      r = extrapolateVehicle(va, lagMs);
      out.degraded = (renderClockMs_ - base.timestampMs) > maxExtrapolationMs_;
      if (out.degraded) {
        r = va;
      }
    } else if (auto it = bmap.find(va.id); it != bmap.end()) {
      r = interpolateVehicle(va, *it->second, bracket.t);
    }

    r.hudSpeed = fadeAlphaForVehicle(r.id, renderClockMs_, true) * r.hudSpeed;
    out.vehicles.push_back(r);
  }

  for (auto& [id, meta] : tracks_) {
    if (!currentIds.count(id) && (renderClockMs_ - meta.lastSeenMs) <= kFadeOutMs) {
      VehicleState ghost;
      ghost.id = id;
      ghost.hudSpeed = meta.filteredHudSpeed * fadeAlphaForVehicle(id, renderClockMs_, false);
      out.vehicles.push_back(ghost);
    }
  }

  out.laneMesh = buildLaneGuideMesh(base.laneCenterline, out.degraded);
  return out;
}

VehicleState SceneFrameInterpolator::interpolateVehicle(const VehicleState& a, const VehicleState& b, float t) {
  VehicleState out = a;
  out.position = lerp(a.position, b.position, t);
  out.orientation = slerp(a.orientation, b.orientation, t);

  auto& meta = tracks_[a.id];
  if (meta.firstSeenMs == 0) meta.firstSeenMs = renderClockMs_;
  meta.lastSeenMs = renderClockMs_;
  const float hud = (1.0F - hudAlpha_) * meta.filteredHudSpeed + hudAlpha_ * b.hudSpeed;
  meta.filteredHudSpeed = hud;
  out.hudSpeed = hud;
  return out;
}

VehicleState SceneFrameInterpolator::extrapolateVehicle(const VehicleState& latest, int64_t dtMs) {
  VehicleState out = latest;
  const float dt = static_cast<float>(dtMs) / 1000.0F;
  out.position = latest.position + latest.velocity * dt;
  auto& meta = tracks_[latest.id];
  if (meta.firstSeenMs == 0) meta.firstSeenMs = renderClockMs_;
  meta.lastSeenMs = renderClockMs_;
  const float hud = (1.0F - hudAlpha_) * meta.filteredHudSpeed + hudAlpha_ * latest.hudSpeed;
  meta.filteredHudSpeed = hud;
  out.hudSpeed = hud;
  return out;
}

float SceneFrameInterpolator::fadeAlphaForVehicle(const std::string& id, int64_t nowMs, bool existsInCurrent) {
  auto& meta = tracks_[id];
  if (meta.firstSeenMs == 0) meta.firstSeenMs = nowMs;
  if (existsInCurrent) {
    meta.lastSeenMs = nowMs;
    return clamp01(static_cast<float>(nowMs - meta.firstSeenMs) / static_cast<float>(kFadeInMs));
  }
  return clamp01(1.0F - static_cast<float>(nowMs - meta.lastSeenMs) / static_cast<float>(kFadeOutMs));
}

LaneGuideMesh SceneFrameInterpolator::buildLaneGuideMesh(const std::vector<LaneGuidePoint>& centerline, bool degraded) const {
  LaneGuideMesh mesh;
  mesh.degraded = degraded;
  if (centerline.size() < 2) return mesh;

  std::vector<float> cumLen(centerline.size(), 0.0F);
  for (std::size_t i = 1; i < centerline.size(); ++i) {
    Vec3 d = centerline[i].center - centerline[i - 1].center;
    cumLen[i] = cumLen[i - 1] + std::sqrt(d.x * d.x + d.y * d.y + d.z * d.z);
  }
  const float total = std::max(1e-3F, cumLen.back());

  mesh.vertices.reserve(centerline.size() * 2);
  for (std::size_t i = 0; i < centerline.size(); ++i) {
    const Vec3 tangent = (i + 1 < centerline.size()) ? (centerline[i + 1].center - centerline[i].center)
                                                      : (centerline[i].center - centerline[i - 1].center);
    const float mag = std::max(1e-3F, std::sqrt(tangent.x * tangent.x + tangent.y * tangent.y));
    Vec3 normal{-tangent.y / mag, tangent.x / mag, 0.0F};

    const Vec3 left = centerline[i].center + normal * centerline[i].halfWidth;
    const Vec3 right = centerline[i].center - normal * centerline[i].halfWidth;
    const float alpha = 1.0F - cumLen[i] / total;
    mesh.vertices.push_back({left, alpha});
    mesh.vertices.push_back({right, alpha});
  }
  return mesh;
}

Vec3 SceneFrameInterpolator::lerp(const Vec3& a, const Vec3& b, float t) { return a + (b - a) * clamp01(t); }

Quat SceneFrameInterpolator::slerp(const Quat& qa, const Quat& qb, float t) {
  Quat a = normalize(qa);
  Quat b = normalize(qb);
  float cosTheta = dot(a, b);
  if (cosTheta < 0.0F) {
    cosTheta = -cosTheta;
    b = {-b.w, -b.x, -b.y, -b.z};
  }
  if (cosTheta > 0.9995F) {
    return normalize({a.w + t * (b.w - a.w), a.x + t * (b.x - a.x), a.y + t * (b.y - a.y), a.z + t * (b.z - a.z)});
  }
  const float angle = std::acos(std::max(-1.0F, std::min(1.0F, cosTheta)));
  const float sinAngle = std::sin(angle);
  const float w0 = std::sin((1.0F - t) * angle) / sinAngle;
  const float w1 = std::sin(t * angle) / sinAngle;
  return normalize({w0 * a.w + w1 * b.w, w0 * a.x + w1 * b.x, w0 * a.y + w1 * b.y, w0 * a.z + w1 * b.z});
}

}  // namespace render


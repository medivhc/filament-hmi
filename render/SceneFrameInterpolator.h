#pragma once

#include <cstdint>
#include <deque>
#include <string>
#include <unordered_map>
#include <vector>

namespace render {

struct Vec3 {
  float x{0.0F};
  float y{0.0F};
  float z{0.0F};

  Vec3 operator+(const Vec3& rhs) const;
  Vec3 operator-(const Vec3& rhs) const;
  Vec3 operator*(float s) const;
};

struct Quat {
  float w{1.0F};
  float x{0.0F};
  float y{0.0F};
  float z{0.0F};
};

struct VehicleState {
  std::string id;
  Vec3 position;
  Vec3 velocity;
  Quat orientation;
  float hudSpeed{0.0F};
};

struct LaneGuidePoint {
  Vec3 center;
  float halfWidth{0.0F};
};

struct LaneGuideMesh {
  struct Vertex {
    Vec3 position;
    float alpha{1.0F};
  };
  std::vector<Vertex> vertices;
  bool degraded{false};
};

struct DrivingSceneFrame {
  int64_t timestampMs{0};
  std::vector<VehicleState> vehicles;
  std::vector<LaneGuidePoint> laneCenterline;
};

struct RenderSceneFrame {
  int64_t timestampMs{0};
  std::vector<VehicleState> vehicles;
  LaneGuideMesh laneMesh;
  bool degraded{false};
};

class SceneFrameInterpolator {
 public:
  explicit SceneFrameInterpolator(std::size_t maxFrames = 20U);

  void pushInputFrame(const DrivingSceneFrame& frame);
  void tickRenderClock();
  RenderSceneFrame renderFrame();

  void setRenderHz(float hz);
  void setExtrapolationThresholdMs(int64_t thresholdMs);
  void setMaxExtrapolationMs(int64_t maxMs);
  void setHudLowPassAlpha(float alpha);

 private:
  static constexpr int64_t kFadeInMs = 150;
  static constexpr int64_t kFadeOutMs = 200;

  struct TrackMeta {
    int64_t firstSeenMs{0};
    int64_t lastSeenMs{0};
    float filteredHudSpeed{0.0F};
  };

  struct BracketFrames {
    const DrivingSceneFrame* f0{nullptr};
    const DrivingSceneFrame* f1{nullptr};
    float t{0.0F};
    bool extrapolated{false};
    bool degraded{false};
  };

  BracketFrames findBracket() const;
  VehicleState interpolateVehicle(const VehicleState& a, const VehicleState& b, float t);
  VehicleState extrapolateVehicle(const VehicleState& latest, int64_t dtMs);
  float fadeAlphaForVehicle(const std::string& id, int64_t nowMs, bool existsInCurrent);
  LaneGuideMesh buildLaneGuideMesh(const std::vector<LaneGuidePoint>& centerline, bool degraded) const;

  static Vec3 lerp(const Vec3& a, const Vec3& b, float t);
  static Quat slerp(const Quat& a, const Quat& b, float t);

  std::size_t maxFrames_;
  std::deque<DrivingSceneFrame> queue_;
  int64_t renderClockMs_{0};
  float renderHz_{60.0F};
  int64_t extrapolationThresholdMs_{120};
  int64_t maxExtrapolationMs_{100};
  float hudAlpha_{0.28F};

  std::unordered_map<std::string, TrackMeta> tracks_;
};

}  // namespace render


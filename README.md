# Filament HMI (Android)

10Hz proto 输入，60Hz Filament 渲染，含 HUD、插值平滑、动态 mesh、测试与 E2E 验收方案。

## 功能
- Filament 生命周期运行时（Engine/Renderer/Scene/View/SwapChain）
- 10Hz -> 60Hz 插值/内推/速度平滑
- proto 字节流反序列化并驱动场景更新
- 道路/蓝带/车辆占位 mesh 组合
- 白模光照与实时日照参数更新
- HUD 叠加（D/红圈/速度/KM/H/ACC/130）
- JVM 单测 + androidTest 烟测 + 语义验收 rubric

## 关键模块
- `render/FilamentRuntime.kt`：生命周期 + 帧调度
- `render/RendererCore.kt`：渲染核心与光照更新
- `scene/DrivingSceneProtoParser.kt`：proto 反序列化
- `render/SceneUpdateEngine.kt`：10Hz ingest + 60Hz compose
- `render/SceneInterpolation.kt`：插值平滑
- `render/SceneMeshes.kt` / `VehiclePlaceholderFactory.kt`：动态 mesh
- `render/HudBinder.kt`：HUD 状态绑定

## 运行测试
```bash
./scripts/test.sh
```

## E2E 验收
1. `./gradlew connectedDebugAndroidTest`
2. `scripts/capture_scene_screenshot.sh`
3. 使用 `docs/e2e/subagent_acceptance_rubric.json` 进行语义评分

## 资产
`app/src/main/assets/models/*.glb` 不纳入 git，请在运行环境注入：
- `ego_white_model.glb`
- `traffic_white_model.glb`


## Troubleshooting: "Pull request object state is invalid"

If Codex Cloud fails while creating a PR with this error, it usually means the branch/base/remote state is incomplete.
Run:

```bash
./scripts/check_pr_ready.sh
```

The script validates:
- current checkout is a branch (not detached HEAD)
- `origin` remote exists and is reachable
- `origin/main` or `origin/master` exists as a PR base
- working tree is clean
- tracked binary extensions are not present
- repository metadata is ready for PR creation

Typical fix flow:

```bash
git remote add origin <your-repo-url>    # if missing

git fetch origin --prune

git push -u origin $(git rev-parse --abbrev-ref HEAD)
```

Then retry PR creation from Codex Cloud.

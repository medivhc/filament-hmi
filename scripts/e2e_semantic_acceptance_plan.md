# E2E Semantic Acceptance Plan (T7)

1. 执行 instrumentation smoke test：
   - `./gradlew connectedDebugAndroidTest`
2. 场景稳定后截图：
   - `scripts/capture_scene_screenshot.sh`
3. 准备输入给 sub-agent：
   - `reference_scene.png`（参考图）
   - `artifacts/render.png`（项目截图）
   - `docs/e2e/subagent_acceptance_rubric.json`（评分规则）
4. sub-agent 输出：
   - 每项 0~5 分、总分、通过/不通过、差异说明
5. 门限：
   - `total_score >= 20` 为通过。

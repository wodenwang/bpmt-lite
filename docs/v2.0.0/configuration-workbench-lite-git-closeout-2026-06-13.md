# Configuration Workbench Lite Git Closeout - 2026-06-13

## Branch

- Current branch: `codex/config-workbench-lite-ui-slice`
- Base: `main`
- Remote: `https://github.com/wodenwang/bpmt-lite.git`
- HEAD: `83f962a Merge pull request #20 from wodenwang/codex/bpmt-lite-v1.7.5-ai-onboarding-prompt`

## Current Git State

当前工作区不干净，包含大量早于本轮或跨版本的未提交改动。第 13 步只完成提交边界整理，未 stage、未 commit、未 push。

## Proposed Commit Boundary

建议第 14 步 `/ship` 先确认是否采用下面的提交边界。

### Commit A: Design and Governance Artifacts

Purpose: 保存低成本配置工作台设计规范、Pencil 原型、评审和计划证据。

Candidate files:

- `DESIGN.md`
- `IMPLEMENTATION_PLAN.md`
- `design/README.md`
- `design/prototype-review.md`
- `design/configuration-workbench-lite-v2.pen`
- `design/configuration-workbench-lite-v2.png`
- `design/configuration-workbench-lite-v2-usage.json`
- `design/qHTYM.png`
- `docs/v2.0.0/configuration-workbench-lite-eng-review.md`
- `docs/v2.0.0/configuration-workbench-lite-design-review-2026-06-13.md`
- `docs/v2.0.0/configuration-workbench-lite-qa-2026-06-13.md`
- `docs/v2.0.0/configuration-workbench-lite-code-review-2026-06-13.md`
- `docs/v2.0.0/configuration-workbench-lite-git-closeout-2026-06-13.md`
- `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/`

### Commit B: UI Slice Implementation

Purpose: 落地第三方系统列表、AI 接入提示词弹窗和 zTree 低成本降噪。

Candidate files:

- `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp`
- `platform/src/main/webapp/css/bpmt-modern.css`

## Files Explicitly Not Included Without Further Review

当前 `git status` 还显示许多不属于本轮最小 UI 切片的改动，例如：

- Maven `pom.xml` 系列
- Docker / install / run 脚本
- API OpenAPI 和文档
- 多个后台列表 JSP
- `README.md`、`AGENTS.md`
- `docs/release-v1.8.0.md`、`docs/release-v1.8.1.md`
- 其他 `docs/v1.8.0/`、`docs/v2.0.0/` 规划材料

这些文件可能来自前置版本工作，但不应在未确认边界前与本次低成本配置工作台切片混合提交。

## Verification Available

- `mvn -s settings.local.xml -DskipTests compile`: `BUILD SUCCESS`
- Playwright verification screenshots:
  - `01-home-ztree.png`
  - `02-thirdpart-list.png`
  - `03-ai-prompt-dialog.png`
- QA report: `docs/v2.0.0/configuration-workbench-lite-qa-2026-06-13.md`
- Code review report: `docs/v2.0.0/configuration-workbench-lite-code-review-2026-06-13.md`

## Next Ship Decision

D1 should decide whether to:

1. stage and commit only the two implementation files plus the focused design/verification artifacts above;
2. include broader v1.8/v2.0 docs already present in the worktree;
3. pause ship and let the user manually separate unrelated work first.

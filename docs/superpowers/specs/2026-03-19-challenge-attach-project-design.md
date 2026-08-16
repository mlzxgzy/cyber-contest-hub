# 挑战编辑页「附加到项目」双向连接设计

**日期:** 2026-03-19
**状态:** 已确认

## 背景

目前项目侧支持「题目管理 → 导入题目」：在项目详情页把题目版本导入到项目（写入 `t_project_challenge`，记录 projectId + challengeId + versionId）。挑战侧没有反向操作。

目标：在挑战编辑页（`/cch/challenge-draft-edit`）新增「附加到项目」Tab，与项目侧形成**双向连接**——两端操作同一张 `t_project_challenge` 关联表，任意一端操作后另一端看到一致结果。

## 显示条件

- 仅当 `challenge.latestVersionId` 非空（挑战已发版入库）时，编辑页显示「附加到项目」Tab
- 未发版 / 新建模式下不显示

## 前端改动（cch-ui）

### challenge-draft-edit/index.vue

- 主区 `NTabs` 增加 `NTabPane name="attach" tab="附加到项目"`，`v-if="challengeData.latestVersionId"`
- 引入 `<ChallengeProjectAttach :challenge-id="challengeId" :latest-version-id="challengeData.latestVersionId" />`

### 新组件 challenge-draft-edit/modules/challenge-project-attach.vue

- **已附加项目列表**：表格展示 项目名称 / 版本号 / 附加时间 / 附加人，行操作「移除」（复用 `fetchRemoveProjectChallenges(projectId, [关联记录id])`）
- **附加操作**：`NSelect` 多选项目（数据源 = `fetchGetProjectList`，与项目列表页同一 API），点「附加」循环调用 `fetchImportProjectChallenges(projectId, [{versionId: latestVersionId}])` 附加当前发版版本；已附加过的项目过滤/置灰

### useTabQuerySync.ts

- `getAvailableMainTabs()` 增加 `attach`，保证刷新后 Tab 定位不丢失

## 后端改动（cch-system）

### 新增查询接口

- `GET /cch/challenge/{challengeId}/projects`
  - 权限：`cch:challenge:query`
  - 实现：`ProjectChallengeMapper` 按 `challenge_id` 查全部版本附加记录 → 填充项目名称
  - 返回：`List<ProjectChallengeVo>`（增加 `projectName` 字段）
  - 位置：`ChallengeController`；Service 方法加在 `IProjectService`/`ProjectServiceImpl`

### 复用现有接口（无改动）

- 附加：`POST /cch/project/{projectId}/challenges`（前端循环调用，后端校验项目管理员权限）
- 移除：`DELETE /cch/project/{projectId}/challenges`（按关联记录 id）

## 权限模型

- 查询已附加列表：`cch:challenge:query`（挑战编辑者可看，无需项目成员身份）
- 附加/移除：复用现有接口，需目标项目管理员权限（与项目侧一致）

## 边界情况

- 已附加过的项目再次附加：后端 `importChallenges` 跳过重复；前端过滤置灰
- 发版后编辑草稿：附加的始终是 `latestVersionId` 对应版本
- 刷新页面：`useTabQuerySync` 保证 Tab 恢复

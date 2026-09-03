<script lang="ts" setup>
import {onMounted, onUnmounted, ref, watch} from 'vue';
import {
  NButton,
  NCard,
  NEmpty,
  NIcon,
  NInputNumber,
  NSelect,
  NSpace,
  NSpin,
  NTag,
  NTabs,
  NTabPane,
  NModal,
  NForm,
  NFormItem
} from 'naive-ui';
import {
  fetchContainerMockTestList,
  fetchContainerMockTestSources,
  fetchDestroyContainerMockTest,
  fetchExtendContainerMockTest,
  fetchStartContainerMockTest,
  type ContainerMockTest
} from '@/service/api/cch/challenge-container-mock-test';

defineOptions({
  name: 'ChallengeContainerMockTest'
});

interface Props {
  currentDraftId?: CommonType.IdType | null;
  challengeId?: CommonType.IdType | null;
}

const props = defineProps<Props>();

// 来源类型
const sourceType = ref<'draft' | 'version'>('draft');
const selectedSourceId = ref<number | null>(null);
const sourceOptions = ref<Array<{label: string; value: number}>>([]);
const loadingSources = ref(false);
const starting = ref(false);

// 活跃测试列表
const mockTests = ref<ContainerMockTest.TestDetail[]>([]);
const loadingTests = ref(false);

// 延长时长弹窗
const extendModalVisible = ref(false);
const extendMinutes = ref(30);
const extendingTestId = ref<number | null>(null);
const extending = ref(false);

// 倒计时定时器
let countdownTimer: ReturnType<typeof setInterval> | null = null;

// 启动中状态轮询定时器
let startingPollTimer: ReturnType<typeof setInterval> | null = null;
const STARTING_POLL_INTERVAL = 3000;
const notifiedFailedIds = new Set<number>();

// 格式化时间
function formatTime(seconds: number): string {
  if (seconds <= 0) return '已过期';

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  if (hours > 0) {
    return `${hours}时${minutes}分${secs}秒`;
  } else if (minutes > 0) {
    return `${minutes}分${secs}秒`;
  }
  return `${secs}秒`;
}

// 格式化完整时间
function formatDateTime(dateStr: string): string {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN');
}

// 时间格式化函数（与修改历史组件相同）
function formatRelativeTime(time: string | Date | null | undefined): string {
  if (!time) {
    return '';
  }

  try {
    const now = new Date();
    const target = new Date(time);

    // 检查日期是否有效
    if (isNaN(target.getTime())) {
      return '';
    }

    const diffMs = now.getTime() - target.getTime();
    const diffHours = diffMs / (1000 * 60 * 60);
    const diffDays = diffMs / (1000 * 60 * 60 * 24);

    if (diffHours < 1) {
      const diffMins = Math.floor(diffMs / (1000 * 60));
      return diffMins <= 0 ? '刚刚' : `${diffMins}分钟前`;
    } else if (diffHours < 24) {
      return `${Math.floor(diffHours)}小时前`;
    } else if (diffDays < 2) {
      return '昨天';
    } else {
      return target.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    }
  } catch (err) {
    return '';
  }
}

// 获取状态类型
function getStatusType(status: string): 'success' | 'warning' | 'error' | 'info' {
  switch (status) {
    case 'running':
      return 'success';
    case 'starting':
      return 'warning';
    case 'destroying':
      return 'warning';
    case 'failed':
      return 'error';
    case 'expired':
      return 'error';
    default:
      return 'info';
  }
}

// 获取状态标签
function getStatusLabel(status: string): string {
  switch (status) {
    case 'starting':
      return '启动中';
    case 'running':
      return '运行中';
    case 'failed':
      return '启动失败';
    case 'destroying':
      return '销毁中';
    case 'expired':
      return '已过期';
    default:
      return status;
  }
}

// 加载来源选项
async function loadSources() {
  // 获取有效的 challengeId（保持字符串格式以避免大整数精度丢失）
  const cid = props.challengeId;

  // 验证 challengeId 是否有效
  if (cid === null || cid === undefined) {
    console.warn('[loadSources] challengeId 未定义，请检查题目是否已保存');
    sourceOptions.value = [];
    return;
  }

  // 转换为字符串，避免大整数精度丢失
  const challengeIdStr = String(cid).trim();
  if (!challengeIdStr || challengeIdStr === '') {
    console.warn('[loadSources] challengeId 是空字符串');
    sourceOptions.value = [];
    return;
  }

  console.log('[loadSources] 开始加载, challengeId:', challengeIdStr, 'sourceType:', sourceType.value);
  loadingSources.value = true;
  try {
    const {data, error} = await fetchContainerMockTestSources(challengeIdStr);
    if (error) {
      window.$message?.error(`获取来源列表失败: ${error}`);
      sourceOptions.value = [];
      return;
    }

    // 根据当前来源类型过滤
    const filtered = (data || []).filter(item => {
      const match = item.sourceType === sourceType.value;
      console.log('[loadSources] 过滤项:', {id: item.id, name: item.name, sourceType: item.sourceType, match});
      return match;
    });

    sourceOptions.value = filtered.map(item => {
      const timeStr = item.createTime ? formatRelativeTime(item.createTime) : '';
      const label = timeStr ? `${item.name} - ${timeStr}` : item.name;
      return {
        label,
        value: item.id
      };
    });

    if (sourceOptions.value.length === 0 && data && data.length > 0) {
      console.warn('[loadSources] 警告: 有数据但过滤后为空, 当前sourceType:', sourceType.value, '数据中的sourceType:', data.map(d => d.sourceType));
    }
  } catch (err) {
    console.error('[loadSources] 异常:', err);
    window.$message?.error(`获取来源列表异常: ${err}`);
    sourceOptions.value = [];
  } finally {
    loadingSources.value = false;
  }
}

// 加载活跃测试列表
async function loadMockTests() {
  loadingTests.value = true;
  try {
    const {data, error} = await fetchContainerMockTestList();
    if (error) {
      window.$message?.error(`获取测试列表失败: ${error}`);
      return;
    }
    mockTests.value = data || [];
    // 存在启动中的测试时开启轮询
    ensureStartingPoll();
  } catch (err) {
    window.$message?.error(`获取测试列表异常: ${err}`);
  } finally {
    loadingTests.value = false;
  }
}

// 静默刷新列表（不触发 loading，用于轮询）
async function silentRefresh() {
  try {
    const {data, error} = await fetchContainerMockTestList();
    if (!error) {
      mockTests.value = data || [];
    }
  } catch {
    // 轮询刷新失败时静默处理，等待下次轮询
  }
}

// 开启启动中状态轮询
function ensureStartingPoll() {
  if (startingPollTimer) return;
  startingPollTimer = setInterval(async () => {
    const hasStarting = mockTests.value.some(test => test.status === 'starting');
    const hasUnnotifiedFailed = mockTests.value.some(
      test => test.status === 'failed' && !notifiedFailedIds.has(test.id)
    );

    if (!hasStarting && !hasUnnotifiedFailed) {
      stopStartingPoll();
      return;
    }

    await silentRefresh();

    // 提示新出现的启动失败
    mockTests.value.forEach(test => {
      if (test.status === 'failed' && !notifiedFailedIds.has(test.id)) {
        notifiedFailedIds.add(test.id);
        window.$message?.error(`测试环境启动失败: ${test.errorMsg || '未知原因'}`);
      }
    });

    if (!mockTests.value.some(test => test.status === 'starting')) {
      stopStartingPoll();
    }
  }, STARTING_POLL_INTERVAL);
}

// 停止启动中状态轮询
function stopStartingPoll() {
  if (startingPollTimer) {
    clearInterval(startingPollTimer);
    startingPollTimer = null;
  }
}

// 启动测试
async function handleStartTest() {
  if (!selectedSourceId.value) {
    window.$message?.warning('请选择要测试的来源');
    return;
  }

  starting.value = true;
  try {
    const {data, error} = await fetchStartContainerMockTest({
      sourceType: sourceType.value,
      sourceId: selectedSourceId.value
    });

    if (error) {
      window.$message?.error(`启动测试失败: ${error}`);
      return;
    }

    window.$message?.success('测试已提交，容器环境正在后台启动，请稍候...');
    selectedSourceId.value = null;
    await loadMockTests();
  } catch (err) {
    window.$message?.error(`启动测试异常: ${err}`);
  } finally {
    starting.value = false;
  }
}

// 打开延长时长弹窗
function openExtendModal(testId: number) {
  extendingTestId.value = testId;
  extendMinutes.value = 30;
  extendModalVisible.value = true;
}

// 确认延长
async function confirmExtend() {
  if (!extendingTestId.value) return;

  extending.value = true;
  try {
    const {error} = await fetchExtendContainerMockTest(extendingTestId.value, extendMinutes.value);
    if (error) {
      window.$message?.error(`延长失败: ${error}`);
      return;
    }

    window.$message?.success(`已延长 ${extendMinutes.value} 分钟`);
    extendModalVisible.value = false;
    await loadMockTests();
  } catch (err) {
    window.$message?.error(`延长异常: ${err}`);
  } finally {
    extending.value = false;
  }
}

// 销毁测试
async function handleDestroy(testId: number) {
  try {
    const {error} = await fetchDestroyContainerMockTest(testId);
    if (error) {
      window.$message?.error(`销毁失败: ${error}`);
      return;
    }

    window.$message?.success('销毁请求已提交，容器正在停止中...');
    await loadMockTests();
  } catch (err) {
    window.$message?.error(`销毁异常: ${err}`);
  }
}

// 复制访问地址
function copyAccessUrl(container: ContainerMockTest.ContainerInfo) {
  const url = getAccessUrl(container);
  navigator.clipboard.writeText(url).then(() => {
    window.$message?.success('已复制到剪贴板');
  }).catch(() => {
    window.$message?.error('复制失败');
  });
}

// 获取完整的访问地址
function getAccessUrl(container: ContainerMockTest.ContainerInfo): string {
  if (!container.host || !container.externalPort) {
    return '-';
  }

  // 根据协议决定 URL 格式
  const protocol = container.protocol?.toLowerCase() || 'tcp';
  if (protocol === 'http') {
    // HTTP 协议使用浏览器可访问的URL格式
    return `http://${container.host}:${container.externalPort}`;
  } else if (protocol === 'tcp') {
    // TCP 协议使用netcat命令格式，方便用户直接复制到终端使用
    return `nc ${container.host} ${container.externalPort}`;
  } else if (protocol === 'udp') {
    // UDP 协议使用netcat UDP命令格式，同样方便终端使用
    return `nc -u ${container.host} ${container.externalPort}`;
  } else {
    // 其他协议保持原有逻辑
    return `${protocol}://${container.host}:${container.externalPort}`;
  }
}

// 获取访问地址显示文本（简化版，用于显示）
function getAccessUrlDisplay(container: ContainerMockTest.ContainerInfo): string {
  if (!container.host || !container.externalPort) {
    return '-';
  }
  return `${container.host}:${container.externalPort}`;
}

// 更新倒计时
function updateCountdown() {
  mockTests.value.forEach(test => {
    if (test.remainingSeconds && test.remainingSeconds > 0) {
      test.remainingSeconds -= 1;
    }
  });
}

// 监听来源类型变化
watch(sourceType, () => {
  selectedSourceId.value = null;
  loadSources();
});

// 监听题目变化
watch(() => props.challengeId, () => {
  selectedSourceId.value = null;
  loadSources();
  loadMockTests();
});

// 组件挂载
onMounted(() => {
  loadSources();
  loadMockTests();

  // 启动倒计时（每秒更新）
  countdownTimer = setInterval(updateCountdown, 1000);
});

// 组件卸载
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
  stopStartingPoll();
});
</script>

<template>
  <div class="container-mock-test-container">
    <!-- 来源类型选择 -->
    <div class="source-type-section">
      <NTabs v-model:value="sourceType" type="card" size="small">
        <NTabPane name="draft" tab="草稿列表"/>
        <NTabPane name="version" tab="版本列表"/>
      </NTabs>
    </div>

    <!-- 选择区域 -->
    <div class="select-section">
      <NSelect
        v-model:value="selectedSourceId"
        :options="sourceOptions"
        :placeholder="sourceType === 'draft' ? '选择要测试的草稿' : '选择要测试的版本'"
        :loading="loadingSources"
        filterable
        clearable
        class="source-select"
      />
      <NButton
        type="primary"
        :loading="starting"
        :disabled="!selectedSourceId"
        @click="handleStartTest"
      >
        <template #icon>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
            <polygon points="5 3 19 12 5 21 5 3"/>
          </svg>
        </template>
        启动测试
      </NButton>
    </div>

    <!-- 活跃测试列表 -->
    <div class="tests-section">
      <div class="section-title">活跃测试环境</div>

      <NSpin :show="loadingTests">
        <div v-if="mockTests.length" class="test-list">
          <NCard
            v-for="test in mockTests"
            :key="test.id"
            class="test-card"
            size="small"
          >
            <template #header>
              <div class="test-card-header">
                <div class="test-title">
                  <span class="title-text">{{ test.challengeName }}</span>
                  <NTag :type="getStatusType(test.status)" size="small">
                    {{ getStatusLabel(test.status) }}
                  </NTag>
                </div>
                <div class="test-source">
                  <NTag :type="test.sourceType === 'version' ? 'warning' : 'info'" size="small">
                    {{ test.sourceType === 'version' ? '版本' : '草稿' }}
                  </NTag>
                  <span class="source-id">ID: {{ test.sourceId }}</span>
                </div>
              </div>
            </template>

            <!-- 启动中提示 -->
            <div v-if="test.status === 'starting'" class="starting-tip">
              <NSpin size="small"/>
              <span>容器环境正在后台启动（可能需要拉取镜像），请稍候...</span>
            </div>

            <!-- 启动失败提示 -->
            <div v-if="test.status === 'failed'" class="failed-tip">
              启动失败: {{ test.errorMsg || '未知原因' }}
            </div>

            <!-- 容器访问信息 -->
            <div v-if="test.containers && test.containers.length" class="container-info">
              <div
                v-for="(container, idx) in test.containers"
                :key="idx"
                class="container-item"
              >
                <div class="container-name">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                    <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
                    <line x1="8" y1="21" x2="16" y2="21"/>
                    <line x1="12" y1="17" x2="12" y2="21"/>
                  </svg>
                  <span>{{ container.name }}</span>
                  <NTag v-if="container.protocol" size="small" type="info" style="margin-left: 4px;">
                    {{ container.protocol.toUpperCase() }}
                  </NTag>
                </div>
                <div class="access-url">
                  <div class="url-display">
                    <code class="full-url">{{ getAccessUrl(container) }}</code>
                    <span class="port-mapping" v-if="container.externalPort">
                      → {{ container.internalPort }}
                    </span>
                  </div>
                  <NButton text size="small" @click="copyAccessUrl(container)" title="复制完整地址">
                    <template #icon>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14"
                           height="14">
                        <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                        <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                      </svg>
                    </template>
                  </NButton>
                </div>
              </div>
            </div>

            <!-- 底部操作栏 -->
            <div class="test-card-footer">
              <div class="countdown">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
                <span>剩余: {{ formatTime(test.remainingSeconds || 0) }}</span>
                <NTag v-if="test.extendCount > 0" size="small" type="warning">
                  已延长{{ test.extendCount }}次
                </NTag>
              </div>
              <NSpace>
                <NButton size="small" @click="openExtendModal(test.id)">
                  延长时间
                </NButton>
                <NButton
                  size="small"
                  type="error"
                  :disabled="test.status === 'destroying' || test.status === 'expired'"
                  @click="handleDestroy(test.id)"
                >
                  销毁
                </NButton>
              </NSpace>
            </div>
          </NCard>
        </div>

        <NEmpty v-else description="暂无活跃的容器测试环境" size="small"/>
      </NSpin>
    </div>

    <!-- 延长时长弹窗 -->
    <NModal v-model:show="extendModalVisible" preset="dialog" title="延长测试时间">
      <NForm>
        <NFormItem label="延长时长（分钟）">
          <NInputNumber
            v-model:value="extendMinutes"
            :min="1"
            :max="120"
            :step="10"
            style="width: 100%"
          />
        </NFormItem>
      </NForm>
      <template #action>
        <NSpace justify="end">
          <NButton @click="extendModalVisible = false">取消</NButton>
          <NButton type="primary" :loading="extending" @click="confirmExtend">
            确认延长
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped lang="scss">
.container-mock-test-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px;
  overflow-y: auto;
}

.source-type-section {
  flex-shrink: 0;
}

.select-section {
  display: flex;
  gap: 8px;
  flex-shrink: 0;

  .source-select {
    flex: 1;
  }
}

.tests-section {
  flex: 1;
  overflow-y: auto;
  min-height: 0;

  .section-title {
    font-size: 14px;
    font-weight: 500;
    color: #666;
    margin-bottom: 8px;
  }
}

.test-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.test-card {
  border-radius: 4px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
    border-color: #d1d5db;
  }

  :deep(.n-card__header) {
    padding: 12px;
    border-bottom: 1px solid #e5e7eb;
    background: #f9fafb;
  }

  :deep(.n-card__content) {
    padding: 12px;
  }
}

.test-card-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.test-title {
  display: flex;
  align-items: center;
  gap: 8px;

  .title-text {
    font-size: 14px;
    font-weight: 600;
    color: #333;
  }
}

.test-source {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999;

  .source-id {
    color: #999;
  }
}

.starting-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  margin-bottom: 12px;
  border: 1px dashed #fbbf24;
  background: #fffbeb;
  border-radius: 4px;
  font-size: 13px;
  color: #b45309;
}

.failed-tip {
  padding: 10px 12px;
  margin-bottom: 12px;
  border: 1px solid #fecaca;
  background: #fef2f2;
  border-radius: 4px;
  font-size: 13px;
  color: #b91c1c;
  word-break: break-all;
}

.container-info {
  margin-bottom: 12px;
}

.container-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #e5e7eb;

  &:last-child {
    border-bottom: none;
  }
}

.container-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #333;
  flex: 1;

  svg {
    color: #3b82f6;
    flex-shrink: 0;
  }

  .port-info {
    font-size: 11px;
    color: #999;
    margin-left: 4px;
  }
}

.access-url {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;

  .url-display {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .full-url {
    font-family: 'Monaco', 'Consolas', monospace;
    font-size: 12px;
    color: #3b82f6;
    background: #f0f7ff;
    padding: 4px 8px;
    border-radius: 2px;
    word-break: break-all;
    max-width: 300px;
    border: 1px solid #dbeafe;
  }

  .port-mapping {
    font-size: 11px;
    color: #666;
    font-family: 'Monaco', 'Consolas', monospace;
  }
}

.test-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;

  svg {
    color: #f59e0b;
  }
}
</style>

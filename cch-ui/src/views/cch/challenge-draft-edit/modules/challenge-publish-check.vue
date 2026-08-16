<script lang="ts" setup>
import {computed} from 'vue';
import {NButton, NCard, NSpace, NTag} from 'naive-ui';

defineOptions({
  name: 'ChallengePublishCheck'
});

interface Props {
  challengeData: Api.Cch.Challenge;
  draftData: Api.Cch.ChallengeDraft | null;
  isCreateMode?: boolean;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'save'): void;
  (e: 'publish'): void;
  (e: 'mock-test'): void;
}

const emit = defineEmits<Emits>();

// 入库状态
const published = computed(() => !!props.challengeData?.latestVersionId);
const created = computed(() => !!props.challengeData?.id);

// 完整性检查清单（与后端发版校验 validatePublishReady 保持一致）
const checks = computed(() => {
  const cfg = props.draftData?.config;
  const runType = cfg?.runType;
  const name = (props.challengeData?.name || '').trim();
  const category = props.challengeData?.category;
  const stem = (cfg?.stem || '').trim();
  const flags = cfg?.flags ?? [];
  const staticFlags = flags.filter(
    f => f && (f as Api.Cch.ChallengeDraftConfigStaticFlag).type === 'static'
  );
  const staticFlagOk = staticFlags.every(
    f => !!((f as Api.Cch.ChallengeDraftConfigStaticFlag).content || '').trim()
  );
  const targets = cfg?.containerTargets ?? [];
  // VM 配置暂存于 config.vmConfig（前端可见，后端待落库）
  const vmName = (cfg as unknown as {vmConfig?: {basic?: {name?: string}}})?.vmConfig?.basic?.name;

  return [
    {
      key: 'basic',
      label: '基本信息',
      desc: '题目名称 / 分类',
      done: !!name && !!category,
      hint: '请填写题目名称并选择题目类型'
    },
    {
      key: 'stem',
      label: '题干描述',
      desc: '选手可见的题目说明',
      done: !!stem,
      hint: '请填写题干描述'
    },
    {
      key: 'flag',
      label: 'Flag 配置',
      desc: '至少 1 个，静态Flag需填写内容',
      done: flags.length > 0 && staticFlagOk,
      hint: flags.length === 0 ? '请至少添加一个Flag' : '存在未填写内容的静态Flag'
    },
    {
      key: 'env',
      label: '运行环境',
      desc: runType === 'container' ? '容器靶机（镜像/端口）' : runType === 'vm' ? '虚拟机靶机' : '静态题目（无需环境）',
      done: runType === 'container'
        ? targets.length > 0 && targets.every(t => !!t && !!t.imageId)
        : runType === 'vm'
          ? !!vmName
          : true,
      hint: runType === 'container'
        ? '请至少配置一个靶机并选择镜像'
        : runType === 'vm'
          ? '请填写虚拟机靶机名称'
          : '静态题目无需运行环境'
    }
  ];
});

const allRequiredDone = computed(() => checks.value.every(c => c.done));

const isContainer = computed(() => props.draftData?.config?.runType === 'container');

function handlePublishClick() {
  if (!created.value) {
    window.$message?.warning('请先创建题目（保存草稿）后再发版');
    return;
  }
  if (!allRequiredDone.value) {
    window.$message?.warning('存在未完成的必填项，请先完善后再发版入库');
    return;
  }
  emit('publish');
}
</script>

<template>
  <div class="publish-check">
    <!-- 入库状态 -->
    <NCard size="small" class="status-card">
      <div class="status-row">
        <span class="status-label">入库状态</span>
        <template v-if="!created">
          <NTag type="warning" size="small">尚未创建</NTag>
          <span class="status-desc">填写基本信息并保存草稿后即可入库</span>
        </template>
        <template v-else-if="published">
          <NTag type="success" size="small">已入库</NTag>
          <span class="status-desc">
            最新版本：{{ challengeData.latestVersionTag || challengeData.latestVersionId }}
          </span>
        </template>
        <template v-else>
          <NTag type="warning" size="small">草稿中</NTag>
          <span class="status-desc">内容完善后点击「发版入库」生成版本号，题目即可被项目/赛事引用</span>
        </template>
      </div>
      <div v-if="isContainer" class="mock-test-tip">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
          <line x1="8" y1="21" x2="16" y2="21"/>
          <line x1="12" y1="17" x2="12" y2="21"/>
        </svg>
        <span>容器题目建议先完成「容器模拟测试」验证环境可用，再发版入库。</span>
        <NButton size="small" type="primary" text @click="emit('mock-test')">去模拟测试</NButton>
      </div>
    </NCard>

    <!-- 完整性检查清单 -->
    <NCard size="small" title="入库前完整性检查" class="check-card">
      <div class="check-list">
        <div v-for="check of checks" :key="check.key" class="check-item" :class="{done: check.done}">
          <div class="check-icon">
            <svg v-if="check.done" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </div>
          <div class="check-info">
            <div class="check-title">{{ check.label }}</div>
            <div class="check-desc">{{ check.done ? check.desc : check.hint }}</div>
          </div>
        </div>
      </div>
    </NCard>

    <!-- 操作 -->
    <NSpace justify="end" :size="12" class="action-bar">
      <NButton :disabled="isCreateMode" @click="emit('save')">
        <template #icon>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
            <polyline points="17 21 17 13 7 13 7 21"/>
            <polyline points="7 3 7 8 15 8"/>
          </svg>
        </template>
        保存草稿
      </NButton>
      <NButton type="primary" :disabled="isCreateMode" @click="handlePublishClick">
        <template #icon>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M5 12h14M12 5l7 7-7 7"/>
          </svg>
        </template>
        发版入库
      </NButton>
    </NSpace>
  </div>
</template>

<style scoped lang="scss">
$primary-color: #3b82f6;
$success-color: #10b981;
$warning-color: #f59e0b;
$danger-color: #ef4444;
$border-color: #e5e7eb;
$border-radius: 4px;
$border-radius-sm: 2px;
$bg-secondary: #f9fafb;
$bg-hover: #f3f4f6;
$shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
$text-primary: #1f2937;
$text-secondary: #6b7280;
$text-muted: #9ca3af;

.publish-check {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 720px;
}

.status-card {
  border-radius: $border-radius;
  border: 1px solid $border-color;

  :deep(.n-card__content) {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
}

.status-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.status-label {
  font-size: 13px;
  font-weight: 600;
  color: $text-primary;
}

.status-desc {
  font-size: 12px;
  color: $text-secondary;
}

.mock-test-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: $bg-secondary;
  border: 1px solid $border-color;
  border-radius: $border-radius-sm;
  font-size: 12px;
  color: $text-secondary;

  svg {
    width: 16px;
    height: 16px;
    color: $primary-color;
    flex-shrink: 0;
  }
}

.check-card {
  border-radius: $border-radius;
  border: 1px solid $border-color;

  :deep(.n-card-header__main) {
    font-size: 14px;
    font-weight: 600;
    color: $text-primary;
  }
}

.check-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.check-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid $border-color;
  border-radius: $border-radius-sm;
  background: $bg-secondary;
  transition: all 0.2s ease;

  &.done {
    border-color: rgba(16, 185, 129, 0.35);
    background: rgba(16, 185, 129, 0.05);
  }
}

.check-icon {
  width: 22px;
  height: 22px;
  min-width: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;

  svg {
    width: 14px;
    height: 14px;
  }

  .check-item:not(.done) & {
    background: rgba(239, 68, 68, 0.1);
    color: $danger-color;
  }

  .check-item.done & {
    background: rgba(16, 185, 129, 0.15);
    color: $success-color;
  }
}

.check-info {
  min-width: 0;
}

.check-title {
  font-size: 13px;
  font-weight: 500;
  color: $text-primary;
  margin-bottom: 2px;
}

.check-desc {
  font-size: 12px;
  color: $text-muted;
}

.action-bar {
  margin-top: 4px;
}
</style>

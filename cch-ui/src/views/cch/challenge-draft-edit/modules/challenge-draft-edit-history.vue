<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue';
import {useRouter} from 'vue-router';
import {NButton, NEmpty, NSpin, NTag} from 'naive-ui';
import {fetchGetChallengeDraftHistory} from '@/service/api/cch/challenge-draft';

defineOptions({
  name: 'ChallengeDraftHistory'
});

interface Props {
  challengeId: CommonType.IdType | null;
  currentDraftId?: CommonType.IdType | null;
  /** 派生的父草稿ID（用于高亮派生的源版本） */
  forkFrom?: CommonType.IdType | null;
}

const props = defineProps<Props>();

const router = useRouter();

const historyList = ref<Api.Cch.ChallengeDraft[]>([]);
const historyLoading = ref(false);

// 根据层级动态计算 RGB 渐变颜色（每级 RGB 值增加 5）
function getDepthColor(depth: number): { borderR: number; borderG: number; borderB: number; bgR: number; bgG: number; bgB: number; dotR: number; dotG: number; dotB: number } {
  // 基础颜色（灰色）
  const baseR = 229;
  const baseG = 231;
  const baseB = 235;
  
  // 每级 RGB 增加 5
  const step = 5;
  
  // 计算边框颜色（逐渐变深）
  const borderR = Math.max(0, Math.min(255, baseR - depth * step));
  const borderG = Math.max(0, Math.min(255, baseG - depth * step));
  const borderB = Math.max(0, Math.min(255, baseB - depth * step));
  
  // 背景色（白色逐渐变淡蓝色调）
  const bgR = 255;
  const bgG = Math.max(240, Math.min(255, 255 - depth * step));
  const bgB = Math.max(240, Math.min(255, 255 - depth * step));
  
  // 圆点颜色（从灰色逐渐变蓝）
  const dotBaseR = 156;
  const dotBaseG = 163;
  const dotBaseB = 175;
  const dotR = Math.max(0, Math.min(255, dotBaseR - depth * step));
  const dotG = Math.max(0, Math.min(255, dotBaseG - depth * step));
  const dotB = Math.max(0, Math.min(255, dotBaseB - depth * step + depth * 10));
  
  return { borderR, borderG, borderB, bgR, bgG, bgB, dotR, dotG, dotB };
}

// 时间格式化函数
function formatTime(time: string | Date | null | undefined): string {
  if (!time) {
    console.warn('[formatTime] time is empty:', time);
    return '未知时间';
  }
  
  try {
    const now = new Date();
    const target = new Date(time);
    
    // 检查日期是否有效
    if (isNaN(target.getTime())) {
      console.warn('[formatTime] invalid date:', time);
      return '无效时间';
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
    console.error('[formatTime] error:', err, time);
    return '时间解析失败';
  }
}

// 计算层级深度
function calculateDepth(draft: Api.Cch.ChallengeDraft, nodeMap: Map<CommonType.IdType, Api.Cch.ChallengeDraft>): number {
  if (!draft.parentId) return 0;
  const parent = nodeMap.get(draft.parentId);
  return parent ? calculateDepth(parent, nodeMap) + 1 : 0;
}

// 时间线数据：按创建时间排序，并计算层级深度和颜色
const historyTimelineData = computed(() => {
  const list = historyList.value;
  if (!list || list.length === 0) return [];
  
  // 创建id到节点的映射
  const nodeMap = new Map<CommonType.IdType, Api.Cch.ChallengeDraft>();
  list.forEach(draft => {
    nodeMap.set(draft.id, draft);
  });
  
  // 按创建时间排序（最新的在前）
  const sorted = [...list].sort((a, b) => {
    const timeA = a.createTime ? new Date(a.createTime).getTime() : 0;
    const timeB = b.createTime ? new Date(b.createTime).getTime() : 0;
    return timeB - timeA;
  });
  
  // 计算每个节点的深度和颜色
  return sorted.map(draft => ({
    ...draft,
    depth: calculateDepth(draft, nodeMap),
    depthColor: getDepthColor(calculateDepth(draft, nodeMap))
  }));
});

// 加载版本历史
async function loadHistoryList() {
  if (!props.challengeId) return;

  historyLoading.value = true;
  try {
    const {data, error} = await fetchGetChallengeDraftHistory(props.challengeId);
    if (error) {
      window.$message?.error(`获取版本历史失败: ${error}`);
      return;
    }
    historyList.value = data || [];
    // 调试：打印第一条数据看看结构
    if (data && data.length > 0) {
      console.log('[loadHistoryList] 第一条数据:', data[0]);
      console.log('[loadHistoryList] createTime:', data[0].createTime);
      console.log('[loadHistoryList] 所有字段:', Object.keys(data[0]));
    }
  } catch (err) {
    window.$message?.error(`获取版本历史异常: ${err}`);
  } finally {
    historyLoading.value = false;
  }
}

// 从指定版本派生新版本（跳转到编辑页面，保存时才创建新版本）
function forkFromVersion(draft: Api.Cch.ChallengeDraft) {
  router.push({
    name: 'cch_challenge-draft-edit',
    query: {
      draftId: draft.id,
      challengeId: draft.challengeId,
      forkFrom: String(draft.id), // 标记这是派生模式
      refresh: 'true',
    }
  });
}

// 暴露方法给父组件
defineExpose({
  refresh: loadHistoryList
});

onMounted(() => {
  loadHistoryList();
});
</script>

<template>
  <NSpin :show="historyLoading">
    <div class="history-container">
      <div v-if="!historyLoading && historyTimelineData.length === 0" class="empty-wrapper">
        <NEmpty description="暂无版本历史"/>
      </div>

      <template v-else>
        <div class="h-summary">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
          共 {{ historyTimelineData.length }} 个版本 · 悬停可「派生」
        </div>
        <div
          v-for="draft in historyTimelineData"
          :key="draft.id"
          class="h-item"
          :class="{
            'is-current': draft.id === currentDraftId,
            'is-fork-source': draft.id === forkFrom
          }"
        >
          <span
            class="h-dot"
            :style="{ background: `rgb(${draft.depthColor.dotR}, ${draft.depthColor.dotG}, ${draft.depthColor.dotB})` }"
          ></span>
          <NTag
            v-if="draft.draftVersion"
            type="info"
            size="small"
            :bordered="false"
            class="h-ver"
          >
            第{{ draft.draftVersion }}版
          </NTag>
          <span class="h-name">{{ draft.challengeName || '未命名题目' }}</span>
          <span class="h-time">{{ draft.createTime ? formatTime(draft.createTime) : '未知时间' }}</span>
          <NTag v-if="draft.id === currentDraftId" type="success" size="small">当前</NTag>
          <NTag v-else-if="draft.id === forkFrom" type="warning" size="small">源版本</NTag>
          <NButton
            v-if="draft.id !== currentDraftId"
            text
            type="primary"
            size="tiny"
            class="h-fork"
            @click="forkFromVersion(draft)"
          >
            派生
          </NButton>
        </div>
      </template>
    </div>
  </NSpin>
</template>

<style scoped lang="scss">
// 紧凑单行列表（由滚动容器接管，组件本身不再限制高度）
.history-container {
  min-height: 120px;
}

.empty-wrapper {
  padding: 32px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.h-summary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  font-size: 11px;
  color: #9ca3af;
  border-bottom: 1px solid #eef0f2;
  background: #fafbfc;

  svg {
    width: 11px;
    height: 11px;
    flex-shrink: 0;
    color: #3b82f6;
  }
}

.h-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-bottom: 1px solid #f3f4f6;
  cursor: default;
  transition: background 0.15s ease;

  &:hover {
    background: #f9fafb;

    .h-fork {
      opacity: 1;
    }
  }

  &.is-current {
    background: #eff6ff;
    box-shadow: inset 2px 0 0 #3b82f6;

    .h-name {
      color: #3b82f6;
      font-weight: 600;
    }

    .h-dot {
      background: #3b82f6 !important;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
    }
  }

  &.is-fork-source {
    background: #fffbeb;
    box-shadow: inset 2px 0 0 #f59e0b;

    .h-dot {
      background: #f59e0b !important;
    }
  }
}

.h-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  background: #d1d5db;
  transition: all 0.2s ease;
}

.h-ver {
  flex-shrink: 0;
}

.h-name {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.h-time {
  font-size: 11px;
  color: #9ca3af;
  flex-shrink: 0;
}

.h-fork {
  opacity: 0;
  transition: opacity 0.15s ease;
  flex-shrink: 0;
}
</style>

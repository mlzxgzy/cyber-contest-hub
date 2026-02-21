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
      refresh: true,
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
      
      <div v-else class="timeline-wrapper">
        <!-- 时间线节点 -->
        <div class="timeline-nodes">
          <div
            v-for="(draft, index) in historyTimelineData"
            :key="draft.id"
            class="timeline-item"
            :class="{
              'is-current': draft.id === currentDraftId,
              'is-fork-source': draft.id === forkFrom
            }"
          >
            <!-- 时间线点 -->
            <div class="timeline-dot-wrapper">
              <div 
                class="timeline-dot"
                :style="{ 
                  background: `rgb(${draft.depthColor.dotR}, ${draft.depthColor.dotG}, ${draft.depthColor.dotB})`,
                  borderColor: `rgb(${draft.depthColor.dotR}, ${draft.depthColor.dotG}, ${draft.depthColor.dotB})`
                }"
              ></div>
              <div v-if="index < historyTimelineData.length - 1" class="timeline-line"></div>
            </div>
            
            <!-- 版本卡片 -->
            <div 
              class="version-card"
              :style="{ 
                borderColor: `rgb(${draft.depthColor.borderR}, ${draft.depthColor.borderG}, ${draft.depthColor.borderB})`,
                background: `rgb(${draft.depthColor.bgR}, ${draft.depthColor.bgG}, ${draft.depthColor.bgB})`
              }"
            >
              <div class="version-content">
                <div class="version-title-row">
                  <span class="version-name">{{ draft.challengeName || '未命名题目' }}</span>
                  <NTag
                    v-if="draft.id === currentDraftId"
                    type="success"
                    size="small"
                  >
                    当前
                  </NTag>
                  <NTag
                    v-if="draft.id === forkFrom && draft.id !== currentDraftId"
                    type="warning"
                    size="small"
                  >
                    源版本
                  </NTag>
                </div>
                <div class="version-time">
                  <template v-if="draft.createTime">
                    {{ formatTime(draft.createTime) }}
                  </template>
                  <template v-else>
                    <span style="color: #ef4444;">无时间信息 (createTime: {{ draft.createTime }})</span>
                  </template>
                </div>
              </div>
              <div class="version-actions">
                <NButton
                  text
                  type="primary"
                  size="small"
                  @click="forkFromVersion(draft)"
                >
                  派生
                </NButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </NSpin>
</template>

<style scoped lang="scss">
.history-container {
  min-height: 200px;
  max-height: calc(100vh - 350px);
  overflow-y: auto;
  padding: 8px;
  position: relative;
}

.empty-wrapper {
  padding: 40px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.timeline-wrapper {
  position: relative;
  padding-left: 48px;
}

.timeline-nodes {
  position: relative;
}

.timeline-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
  position: relative;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  &.is-current {
    .version-card {
      border-color: #3b82f6 !important;
      background: rgba(59, 130, 246, 0.1) !important;
      
      .version-name {
        color: #3b82f6;
        font-weight: 600;
      }
    }
    
    .timeline-dot {
      background: #3b82f6 !important;
      border-color: #3b82f6 !important;
      box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.2);
    }
  }
  
  &.is-fork-source {
    .version-card {
      border-color: #f59e0b !important;
      background: rgba(245, 158, 11, 0.1) !important;
      
      .version-name {
        color: #f59e0b;
        font-weight: 600;
      }
    }
    
    .timeline-dot {
      background: #f59e0b !important;
      border-color: #f59e0b !important;
      box-shadow: 0 0 0 4px rgba(245, 158, 11, 0.2);
    }
  }
}

.timeline-dot-wrapper {
  position: absolute;
  left: -48px;
  top: 0;
  width: 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ffffff;
  border: 2px solid #d1d5db;
  flex-shrink: 0;
  z-index: 3;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: #e5e7eb;
  margin-top: 4px;
  min-height: 36px;
}

.version-card {
  flex: 1;
  border: 1px solid;
  border-radius: 4px;
  padding: 12px 14px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  transition: all 0.2s ease;
  min-width: 0;
  min-height: 56px;
  background: #ffffff;
  border-color: #e5e7eb;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  
  &:hover {
    box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
    border-color: #d1d5db;
  }
}

.version-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  gap: 4px;
}

.version-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.version-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.version-time {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
  display: block;
  margin-top: 2px;
}

.version-actions {
  flex-shrink: 0;
}
</style>

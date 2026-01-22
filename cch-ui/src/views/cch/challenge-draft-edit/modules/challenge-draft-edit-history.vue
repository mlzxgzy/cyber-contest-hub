<script lang="ts" setup>
import {computed, h, ref} from 'vue';
import {useRouter} from 'vue-router';
import {NButton, NEmpty, NSpin, NTag, NTree} from 'naive-ui';
import type {TreeOption} from 'naive-ui';
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

// 历史树数据
const historyTreeData = computed(() => {
  // 构建树状结构
  const list = historyList.value;
  if (!list || list.length === 0) return [];

  // 创建id到节点的映射
  const nodeMap = new Map<CommonType.IdType, TreeOption>();

  // 先创建所有节点
  list.forEach(draft => {
    nodeMap.set(draft.id, {
      key: draft.id,
      label: `版本 ${draft.id}`,
      draft,
      children: []
    });
  });

  // 构建树结构
  const rootNodes: TreeOption[] = [];
  list.forEach(draft => {
    const node = nodeMap.get(draft.id)!;
    if (draft.parentId) {
      const parentNode = nodeMap.get(draft.parentId);
      if (parentNode) {
        if (!parentNode.children) {
          parentNode.children = [];
        }
        parentNode.children.push(node);
      } else {
        // 如果父节点不存在，则作为根节点
        rootNodes.push(node);
      }
    } else {
      // 没有父节点的作为根节点
      rootNodes.push(node);
    }
  });

  return rootNodes;
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

// 渲染历史树节点标签
function renderHistoryLabel({option}: { option: TreeOption }) {
  const draft = (option as any).draft as Api.Cch.ChallengeDraft;
  if (!draft) return null;

  // 高亮当前版本或派生的父版本
  const isCurrent = draft.id === props.currentDraftId;
  const isForkSource = draft.id === props.forkFrom;
  const createTime = draft.createTime ? new Date(draft.createTime).toLocaleString('zh-CN') : '';

  // 标签：如果是被派生的源，显示"源版本"；如果是当前版本，显示"当前版本"
  const showCurrentTag = isCurrent && !isForkSource;
  const showForkSourceTag = isForkSource && !isCurrent;

  return h(
    'div',
    {
      class: 'flex items-center justify-between gap-8px w-full py-4px',
      style: {
        padding: '4px 0'
      }
    },
    [
      h('div', {class: 'flex-1'}, [
        h('div', {class: 'flex items-center gap-8px'}, [
          h(
            'span',
            {
              class: isCurrent || isForkSource ? 'font-bold' : '',
              style: {fontSize: '14px', color: isCurrent ? 'var(--primary-color)' : isForkSource ? 'var(--warning-color)' : ''}
            },
            `版本 ${draft.id}`
          ),
          isCurrent ? h(NTag, {type: 'success', size: 'small'}, {default: () => '当前'}) : null,
          isForkSource ? h(NTag, {type: 'warning', size: 'small'}, {default: () => '源版本'}) : null
        ]),
        h(
          'div',
          {
            class: 'text-xs text-gray-500 mt-4px',
            style: {fontSize: '12px', color: '#999'}
          },
          createTime
        )
      ]),
      h(
        NButton,
        {
          text: true,
          type: 'primary',
          size: 'small',
          onClick: (e: Event) => {
            e.stopPropagation();
            forkFromVersion(draft);
          }
        },
        {default: () => '派生'}
      )
    ]
  );
}

// 暴露方法给父组件
defineExpose({
  refresh: loadHistoryList
});
</script>

<template>
  <NSpin :show="historyLoading">
    <div class="history-container">
      <NTree
        :data="historyTreeData"
        key-field="key"
        label-field="label"
        block-node
        :render-label="renderHistoryLabel"
        :default-expanded-keys="
          historyTreeData.map(node => node.key).filter((key): key is CommonType.IdType => key !== undefined)
        "
      />
      <NEmpty v-if="!historyLoading && historyList.length === 0" description="暂无版本历史" class="mt-20px"/>
    </div>
  </NSpin>
</template>

<style scoped lang="scss">
.history-container {
  min-height: 200px;
  max-height: calc(100vh - 350px);
  overflow-y: auto;
  padding: 8px;
}
</style>

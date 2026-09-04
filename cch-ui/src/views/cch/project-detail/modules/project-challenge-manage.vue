<script lang="ts" setup>
import {computed, h, ref, watch} from 'vue';
import {useRouter} from 'vue-router';
import type {DataTableColumns} from 'naive-ui';
import {NButton, NTag} from 'naive-ui';
import {
  fetchGetProjectChallenges,
  fetchGetProjectDetail,
  fetchRemoveProjectChallenges,
  fetchUpdateProjectChallengeTags
} from '@/service/api/cch/project';
import {useAuthStore} from '@/store/modules/auth';
import {useDict} from '@/hooks/business/dict';

defineOptions({
  name: 'ProjectChallengeManage'
});

type PermissionType = 'admin' | 'view_all' | 'view_own';

interface Props {
  projectId: CommonType.IdType;
  isProjectAdmin?: boolean;
  currentPermissionType?: PermissionType | null;
  isSuperAdmin?: boolean;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'refresh'): void;
}

const emit = defineEmits<Emits>();

const router = useRouter();
const authStore = useAuthStore();
const currentUserId = computed(() => authStore.userInfo.user?.userId);

// 题目类型字典（与题目列表页保持一致）
const {options: categoryDictOptions} = useDict('cch_question_categroy');

const loading = ref(false);
const challenges = ref<Api.Cch.ProjectChallenge[]>([]);
const project = ref<Api.Cch.Project | null>(null);

// 勾选的关联记录ID（t_project_challenge.id）
const checkedRowKeys = ref<CommonType.IdType[]>([]);
const tagLoading = ref(false);
const customTag = ref('');

// 标签筛选项（多选，题目需包含全部所选标签才展示）
const filterTags = ref<string[]>([]);

// 题目类型筛选项（多选，题目类型命中任意一个即展示）
const filterCategories = ref<string[]>([]);

// 是否具备针对所有题目的管理能力（删除任意题目、打标签）
const canManageAll = computed(() => {
  return !!(props.isSuperAdmin || props.isProjectAdmin || props.currentPermissionType === 'admin');
});

// 是否有导入能力：所有项目成员以及系统超管都可以导入题目
const canImport = computed(() => {
  if (props.isSuperAdmin) return true;
  return !!props.currentPermissionType;
});

// 是否可以查看所有导入的题目
const canViewAll = computed(() => {
  return canManageAll.value || props.currentPermissionType === 'view_all';
});

// 实际展示的题目列表
const displayChallenges = computed<Api.Cch.ProjectChallenge[]>(() => {
  if (canViewAll.value) {
    return challenges.value;
  }

  // view_own：只展示自己导入的题目
  if (props.currentPermissionType === 'view_own') {
    if (!currentUserId.value) {
      return [];
    }
    return challenges.value.filter(item => item.createBy === currentUserId.value);
  }

  return challenges.value;
});

// 当前列表所有题目的标签汇总（去重），作为筛选选项
const allTagOptions = computed<string[]>(() => {
  const set = new Set<string>();
  displayChallenges.value.forEach(row => {
    parseTags(row.tags).forEach(tag => set.add(tag));
  });
  return Array.from(set).sort((a, b) => a.localeCompare(b, 'zh-CN'));
});

// 按标签筛选后的题目列表：需包含全部所选标签
const filteredChallenges = computed<Api.Cch.ProjectChallenge[]>(() => {
  let result = displayChallenges.value;

  if (filterCategories.value.length > 0) {
    result = result.filter(row => row.category && filterCategories.value.includes(row.category));
  }

  if (filterTags.value.length === 0) {
    return result;
  }
  return result.filter(row => {
    const tags = parseTags(row.tags);
    return filterTags.value.every(tag => tags.includes(tag));
  });
});

// 字典 value -> label 映射，用于展示题目类型名称
const categoryLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {};
  categoryDictOptions.value.forEach(option => {
    map[String(option.value)] = option.label;
  });
  return map;
});

function renderRowCategory(row: Api.Cch.ProjectChallenge) {
  if (!row.category) {
    return h('span', {class: 'text-gray-300'}, {default: () => '—'});
  }
  return h(
    NTag,
    {
      size: 'small',
      round: true,
      bordered: false,
      type: 'info'
    },
    {default: () => categoryLabelMap.value[row.category] || row.category}
  );
}

// 是否需要展示"操作"列：有导入或删除能力的成员都需要看到
const canShowOperateColumn = computed(() => {
  return canImport.value || canManageAll.value;
});

// 快捷标签：阶段名称（来自项目 meta.stages）
const stageTags = computed<string[]>(() => {
  const stages = project.value?.meta?.stages || [];
  return stages
    .map(item => (item.stageName || '').trim())
    .filter(Boolean);
});

// 快捷标签：项目负责人
const leaderTag = computed<string>(() => {
  return (project.value?.leader || '').trim();
});

function parseTags(tags?: string | null): string[] {
  if (!tags) return [];
  return tags
    .split(',')
    .map(item => item.trim())
    .filter(Boolean);
}

const columns = computed<DataTableColumns<Api.Cch.ProjectChallenge>>(() => {
  const baseColumns: DataTableColumns<Api.Cch.ProjectChallenge> = [];

  // 管理员可勾选批量打标签
  if (canManageAll.value) {
    baseColumns.push({type: 'selection'});
  }

  baseColumns.push(
    {
      key: 'category',
      title: '题目类型',
      align: 'center',
      minWidth: 100,
      render: row => renderRowCategory(row)
    },
    {
      key: 'challengeName',
      title: '题目名称',
      align: 'center',
      minWidth: 150
    },
    {
      key: 'versionTag',
      title: '版本号',
      align: 'center',
      minWidth: 120
    },
    {
      key: 'tags',
      title: '标签',
      align: 'center',
      minWidth: 180,
      render: row => renderRowTags(row)
    },
    {
      key: 'createTime',
      title: '导入时间',
      align: 'center',
      minWidth: 160
    }
  );

  if (canShowOperateColumn.value) {
    baseColumns.push({
      key: 'operate',
      title: '操作',
      align: 'center',
      width: 100,
      render: row => {
        const isOwner = row.createBy === currentUserId.value;

        // admin 或系统超级管理员：可删除任意题目
        if (!canManageAll.value && !isOwner) {
          return null;
        }

        return h(
          NButton,
          {
            text: true,
            type: 'error',
            size: 'small',
            onClick: () => handleRemoveChallenge(row.id)
          },
          {default: () => '移除'}
        );
      }
    });
  }

  return baseColumns;
});

function renderRowTags(row: Api.Cch.ProjectChallenge) {
  const tags = parseTags(row.tags);
  if (tags.length === 0) {
    return h('span', {class: 'text-gray-300'}, {default: () => '—'});
  }

  return h(
    'div',
    {class: 'flex flex-wrap justify-center gap-4px'},
    {
      default: () =>
        tags.map(tag =>
          h(
            NTag,
            {
              key: tag,
              size: 'small',
              round: true,
              closable: canManageAll.value,
              onClose: canManageAll.value ? () => handleRemoveTag(row, tag) : undefined
            },
            {default: () => tag}
          )
        )
    }
  );
}

async function loadChallenges() {
  loading.value = true;
  const {data, error} = await fetchGetProjectChallenges(props.projectId);
  if (!error && data) {
    challenges.value = data;
  }
  loading.value = false;
}

// 加载项目信息（用于生成快捷标签：阶段名称、负责人等）
async function loadProject() {
  const {data, error} = await fetchGetProjectDetail(props.projectId);
  if (!error && data) {
    project.value = data;
  }
}

async function handleRemoveChallenge(challengeId: CommonType.IdType) {
  const {error} = await fetchRemoveProjectChallenges(props.projectId, [challengeId]);
  if (error) return;

  window.$message?.success('移除成功');
  await loadChallenges();
  emit('refresh');
}

/**
 * 给勾选的题目追加标签（合并去重）
 */
async function handleAddTag(tag: string) {
  const cleaned = sanitizeTag(tag);
  if (!cleaned) {
    window.$message?.warning('标签不能为空或包含逗号');
    return;
  }

  const rows = displayChallenges.value.filter(item => checkedRowKeys.value.includes(item.id));
  if (rows.length === 0) return;

  // 所选题目全部已包含该标签时，跳过
  if (rows.every(row => parseTags(row.tags).includes(cleaned))) {
    window.$message?.info('所选题目已包含该标签');
    return;
  }

  tagLoading.value = true;
  const {error} = await fetchUpdateProjectChallengeTags(props.projectId, {
    ids: checkedRowKeys.value,
    tags: [cleaned],
    append: true
  });
  tagLoading.value = false;
  if (error) return;

  window.$message?.success(`已为 ${rows.length} 道题目添加标签「${cleaned}」`);
  customTag.value = '';
  checkedRowKeys.value = [];
  await loadChallenges();
  emit('refresh');
}

/**
 * 移除单条记录上的某个标签（覆盖模式传剩余标签）
 */
async function handleRemoveTag(row: Api.Cch.ProjectChallenge, tag: string) {
  const remaining = parseTags(row.tags).filter(item => item !== tag);

  const {error} = await fetchUpdateProjectChallengeTags(props.projectId, {
    ids: [row.id],
    tags: remaining,
    append: false
  });
  if (error) return;

  window.$message?.success(`已移除标签「${tag}」`);
  await loadChallenges();
  emit('refresh');
}

function handleAddCustomTag() {
  void handleAddTag(customTag.value);
}

function sanitizeTag(tag: string) {
  const trimmed = tag.trim();
  if (!trimmed || trimmed.includes(',') || trimmed.includes('，')) {
    return '';
  }
  return trimmed.slice(0, 50);
}

function navigateToImportPage() {
  router.push({
    name: 'cch-project-challenge-import',
    params: { id: props.projectId }
  });
}

watch(() => props.projectId, () => {
  if (props.projectId) {
    loadChallenges();
    loadProject();
  }
}, {immediate: true});

// 切换筛选条件时清空勾选，避免对被隐藏的题目误操作
watch([filterTags, filterCategories], () => {
  checkedRowKeys.value = [];
});

</script>

<template>
  <div class="flex-col-stretch gap-16px">
    <NCard v-if="canImport" :bordered="false" size="small" title="导入题目">
      <NButton type="primary" @click="navigateToImportPage">导入题目</NButton>
    </NCard>

    <NCard :bordered="false" size="small" title="题目列表">
      <div class="mb-12px flex items-center gap-12px">
        <span class="text-12px text-gray-400">类型筛选</span>
        <NSelect
          v-model:value="filterCategories"
          class="w-320px"
          size="small"
          multiple
          clearable
          placeholder="按题目类型筛选题目"
          :options="categoryDictOptions"
          :max-tag-count="3"
        />
      </div>

      <div v-if="allTagOptions.length > 0" class="mb-12px flex items-center gap-12px">
        <span class="text-12px text-gray-400">标签筛选</span>
        <NSelect
          v-model:value="filterTags"
          class="w-320px"
          size="small"
          multiple
          clearable
          placeholder="按标签筛选题目（需包含全部所选标签）"
          :options="allTagOptions.map(tag => ({ label: tag, value: tag }))"
          :max-tag-count="3"
        />
      </div>

      <div v-if="canManageAll" class="mb-12px flex items-center gap-12px">
        <span class="text-12px text-gray-400">已选 {{ checkedRowKeys.length }} 项</span>
        <NPopover trigger="click" placement="bottom-start" :show-arrow="false">
          <template #trigger>
            <NButton
              size="small"
              type="primary"
              ghost
              :loading="tagLoading"
              :disabled="checkedRowKeys.length === 0"
            >
              快速添加标签
            </NButton>
          </template>
          <div class="w-300px flex flex-col gap-12px">
            <div v-if="stageTags.length">
              <div class="mb-6px text-12px text-gray-400">阶段</div>
              <NSpace size="small">
                <NTag
                  v-for="tag in stageTags"
                  :key="`stage-${tag}`"
                  size="small"
                  round
                  :color="{ borderColor: '#36ad6a', textColor: '#36ad6a' }"
                  class="cursor-pointer"
                  @click="handleAddTag(tag)"
                >
                  {{ tag }}
                </NTag>
              </NSpace>
            </div>
            <div v-if="leaderTag">
              <div class="mb-6px text-12px text-gray-400">负责人</div>
              <NSpace size="small">
                <NTag
                  size="small"
                  round
                  :color="{ borderColor: '#2080f0', textColor: '#2080f0' }"
                  class="cursor-pointer"
                  @click="handleAddTag(leaderTag)"
                >
                  {{ leaderTag }}
                </NTag>
              </NSpace>
            </div>
            <div>
              <div class="mb-6px text-12px text-gray-400">自定义标签</div>
              <div class="flex gap-8px">
                <NInput
                  v-model:value="customTag"
                  size="small"
                  placeholder="输入标签，回车添加"
                  maxlength="50"
                  clearable
                  @keydown.enter.prevent="handleAddCustomTag"
                />
                <NButton size="small" @click="handleAddCustomTag">添加</NButton>
              </div>
            </div>
          </div>
        </NPopover>
      </div>

      <NSpin :show="loading">
        <NDataTable
          v-model:checked-row-keys="checkedRowKeys"
          :columns="columns"
          :data="filteredChallenges"
          :row-key="row => row.id"
          size="small"
        />
      </NSpin>
    </NCard>
  </div>
</template>

<style scoped></style>

<script lang="ts" setup>
import {computed, h, ref, watch} from 'vue';
import type {DataTableColumns} from 'naive-ui';
import {NButton} from 'naive-ui';
import {fetchGetChallengeVersionList} from '@/service/api/cch/challenge-version';
import {fetchGetProjectChallenges, fetchImportProjectChallenges, fetchRemoveProjectChallenges} from '@/service/api/cch/project';
import {useAuthStore} from '@/store/modules/auth';
import {$t} from '@/locales';

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

const authStore = useAuthStore();
const currentUserId = computed(() => authStore.userInfo.user?.userId);

const loading = ref(false);
const challenges = ref<Api.Cch.ProjectChallenge[]>([]);

const importModalVisible = ref(false);
const importLoading = ref(false);
const selectedVersionId = ref<CommonType.IdType | null>(null);
const versionOptions = ref<Array<{label: string; value: CommonType.IdType}>>([]);
const versionSearchKeyword = ref('');

// 是否具备针对所有题目的管理能力（删除任意题目）
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

// 是否需要展示“操作”列：有导入或删除能力的成员都需要看到
const canShowOperateColumn = computed(() => {
  return canImport.value || canManageAll.value;
});

const columns = computed<DataTableColumns<Api.Cch.ProjectChallenge>>(() => {
  const baseColumns: DataTableColumns<Api.Cch.ProjectChallenge> = [
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
      key: 'createTime',
      title: '导入时间',
      align: 'center',
      minWidth: 160
    }
  ];

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

async function loadChallenges() {
  loading.value = true;
  const {data, error} = await fetchGetProjectChallenges(props.projectId);
  if (!error && data) {
    challenges.value = data;
  }
  loading.value = false;
}

async function loadVersionOptions() {
  importLoading.value = true;
  const {data, error} = await fetchGetChallengeVersionList({
    pageNum: 1,
    pageSize: 1000,
    versionTag: versionSearchKeyword.value || null,
    challengeName: versionSearchKeyword.value || null,
    params: {}
  });
  
  if (!error && data?.rows) {
    versionOptions.value = data.rows.map(item => ({
      label: `${item.challengeName} - ${item.versionTag}`,
      value: item.id
    }));
  }
  importLoading.value = false;
}

async function handleImportChallenge() {
  if (!selectedVersionId.value) {
    window.$message?.warning('请选择题目版本');
    return;
  }

  // 检查是否已经导入
  if (challenges.value.some(c => c.versionId === selectedVersionId.value)) {
    window.$message?.warning('该题目版本已经导入');
    return;
  }

  const {error} = await fetchImportProjectChallenges(props.projectId, [{
    versionId: selectedVersionId.value
  }]);

  if (error) return;

  window.$message?.success('导入成功');
  selectedVersionId.value = null;
  importModalVisible.value = false;
  await loadChallenges();
  emit('refresh');
}

async function handleRemoveChallenge(challengeId: CommonType.IdType) {
  const {error} = await fetchRemoveProjectChallenges(props.projectId, [challengeId]);
  if (error) return;

  window.$message?.success('移除成功');
  await loadChallenges();
  emit('refresh');
}

function openImportModal() {
  importModalVisible.value = true;
  versionSearchKeyword.value = '';
  selectedVersionId.value = null;
  loadVersionOptions();
}

watch(() => versionSearchKeyword.value, () => {
  loadVersionOptions();
});

watch(() => props.projectId, () => {
  if (props.projectId) {
    loadChallenges();
  }
}, {immediate: true});

</script>

<template>
  <div class="flex-col-stretch gap-16px">
    <NCard v-if="canImport" :bordered="false" size="small" title="导入题目">
      <NButton type="primary" @click="openImportModal">导入题目</NButton>
    </NCard>

    <NCard :bordered="false" size="small" title="题目列表">
      <NSpin :show="loading">
        <NDataTable
          :columns="columns"
          :data="displayChallenges"
          :row-key="row => row.id"
          size="small"
        />
      </NSpin>
    </NCard>

    <NModal
      v-model:show="importModalVisible"
      preset="dialog"
      title="导入题目"
      positive-text="确认"
      negative-text="取消"
      :loading="importLoading"
      @positive-click="handleImportChallenge"
    >
      <NForm label-placement="left" :label-width="100">
        <NFormItem label="搜索题目">
          <NInput
            v-model:value="versionSearchKeyword"
            placeholder="输入题目名称或版本号搜索"
            clearable
          />
        </NFormItem>
        <NFormItem label="选择版本">
          <NSelect
            v-model:value="selectedVersionId"
            :options="versionOptions"
            :loading="importLoading"
            filterable
            placeholder="请选择题目版本"
            clearable
          />
        </NFormItem>
      </NForm>
    </NModal>
  </div>
</template>

<style scoped></style>

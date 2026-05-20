<script lang="ts" setup>
import {computed, h, ref, watch} from 'vue';
import {useRouter} from 'vue-router';
import type {DataTableColumns} from 'naive-ui';
import {NButton} from 'naive-ui';
import {fetchGetProjectChallenges, fetchRemoveProjectChallenges} from '@/service/api/cch/project';
import {useAuthStore} from '@/store/modules/auth';

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

const loading = ref(false);
const challenges = ref<Api.Cch.ProjectChallenge[]>([]);

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

async function handleRemoveChallenge(challengeId: CommonType.IdType) {
  const {error} = await fetchRemoveProjectChallenges(props.projectId, [challengeId]);
  if (error) return;

  window.$message?.success('移除成功');
  await loadChallenges();
  emit('refresh');
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
  }
}, {immediate: true});

</script>

<template>
  <div class="flex-col-stretch gap-16px">
    <NCard v-if="canImport" :bordered="false" size="small" title="导入题目">
      <NButton type="primary" @click="navigateToImportPage">导入题目</NButton>
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
  </div>
</template>

<style scoped></style>

<script lang="ts" setup>
import {computed, h, ref, watch} from 'vue';
import type {DataTableColumns} from 'naive-ui';
import {NButton} from 'naive-ui';
import {fetchGetChallengeVersionList} from '@/service/api/cch/challenge-version';
import {fetchGetProjectChallenges, fetchImportProjectChallenges, fetchRemoveProjectChallenges} from '@/service/api/cch/project';
import {useAuth} from '@/hooks/business/auth';
import {$t} from '@/locales';

defineOptions({
  name: 'ProjectChallengeManage'
});

interface Props {
  projectId: CommonType.IdType;
  isProjectAdmin?: boolean;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'refresh'): void;
}

const emit = defineEmits<Emits>();

const {hasAuth} = useAuth();

const loading = ref(false);
const challenges = ref<Api.Cch.ProjectChallenge[]>([]);

const importModalVisible = ref(false);
const importLoading = ref(false);
const selectedVersionId = ref<CommonType.IdType | null>(null);
const versionOptions = ref<Array<{label: string; value: CommonType.IdType}>>([]);
const versionSearchKeyword = ref('');

const canManage = computed(() => {
  return props.isProjectAdmin && hasAuth('cch:project:challenge');
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

  if (canManage.value) {
    baseColumns.push({
      key: 'operate',
      title: '操作',
      align: 'center',
      width: 100,
      render: row =>
        h(
          NButton,
          {
            text: true,
            type: 'error',
            size: 'small',
            onClick: () => handleRemoveChallenge(row.id)
          },
          {default: () => '移除'}
        )
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
    <NCard v-if="canManage" :bordered="false" size="small" title="导入题目">
      <NButton type="primary" @click="openImportModal">导入题目</NButton>
    </NCard>

    <NCard :bordered="false" size="small" title="题目列表">
      <NSpin :show="loading">
        <NDataTable
          :columns="columns"
          :data="challenges"
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

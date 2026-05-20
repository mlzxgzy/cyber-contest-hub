<script lang="ts" setup>
import {computed, h, ref, watch} from 'vue';
import type {DataTableColumns} from 'naive-ui';
import {NButton, NInput, NSelect} from 'naive-ui';
import {useRoute, useRouter} from 'vue-router';
import {
  fetchAddProjectMembers,
  fetchGenerateProjectInviteCode,
  fetchGetProjectMembers,
  fetchRemoveProjectMembers
} from '@/service/api/cch/project';
import {useAuthStore} from '@/store/modules/auth';

defineOptions({
  name: 'ProjectMemberManage'
});

interface Props {
  projectId: CommonType.IdType;
  isProjectAdmin?: boolean;
  canManageMembers?: boolean;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'refresh'): void;
}

const emit = defineEmits<Emits>();

const route = useRoute();
const router = useRouter();

const authStore = useAuthStore();
const currentUserId = computed(() => authStore.userInfo.user?.userId);

const loading = ref(false);
const members = ref<Api.Cch.ProjectMember[]>([]);

const permissionTypeOptions = [
  {label: '管理员', value: 'admin'},
  {label: '查看所有题', value: 'view_all'},
  {label: '仅查看自己导入的题', value: 'view_own'}
];

// 邀请链接相关
const invitePermissionType = ref<'admin' | 'view_all' | 'view_own'>('view_all');
const inviteLink = ref('');

const canManage = computed(() => {
  return !!props.canManageMembers;
});

const columns = computed<DataTableColumns<Api.Cch.ProjectMember>>(() => {
  const baseColumns: DataTableColumns<Api.Cch.ProjectMember> = [
    {
      key: 'userName',
      title: '用户名',
      align: 'center',
      minWidth: 120
    },
    {
      key: 'nickName',
      title: '昵称',
      align: 'center',
      minWidth: 120
    },
    {
      key: 'permissionType',
      title: '权限类型',
      align: 'center',
      minWidth: 180,
      render: row => {
        if (!canManage.value) {
          return getPermissionTypeLabel(row.permissionType);
        }
        return h(NSelect, {
          value: row.permissionType,
          options: permissionTypeOptions,
          size: 'small',
          style: 'width: 160px',
          'onUpdate:value': (val: 'admin' | 'view_all' | 'view_own') =>
            handleChangePermission(row.userId, val)
        });
      }
    },
    {
      key: 'createTime',
      title: '加入时间',
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
      render: row => {
        // 不能移除自己
        if (row.userId === currentUserId.value) {
          return null;
        }

        return h(
          NButton,
          {
            text: true,
            type: 'error',
            size: 'small',
            onClick: () => handleRemoveMember(row.userId)
          },
          {default: () => '移除'}
        );
      }
    });
  }

  return baseColumns;
});

async function loadMembers() {
  loading.value = true;
  const {data, error} = await fetchGetProjectMembers(props.projectId);
  if (!error && data) {
    members.value = data;
  }
  loading.value = false;
}

async function handleGenerateInviteLink() {
  if (!props.projectId) {
    window.$message?.error('项目ID不能为空');
    return;
  }

  const {
    error,
    response: {data: {msg: data}}
  } = await fetchGenerateProjectInviteCode(props.projectId, invitePermissionType.value);
  if (error || !data) {
    window.$message?.warning('生成邀请码时出现问题');
    return;
  }

  // 使用当前路由信息拼接前端可访问的邀请链接
  const resolved = router.resolve({
    name: 'cch-project-detail',
    params: {id: props.projectId},
    query: {invite: data}
  });
  const origin = window.location.origin;
  inviteLink.value = origin + resolved.href;

  window.$message?.success('邀请链接已生成');
}

async function handleCopyInviteLink() {
  if (!inviteLink.value) {
    window.$message?.warning('请先生成邀请链接');
    return;
  }

  try {
    await navigator.clipboard.writeText(inviteLink.value);
    window.$message?.success('邀请链接已复制到剪贴板');
  } catch {
    window.$message?.error('复制失败，请手动复制');
  }
}

async function handleChangePermission(userId: CommonType.IdType, permissionType: 'admin' | 'view_all' | 'view_own') {
  const {error} = await fetchAddProjectMembers(props.projectId, [
    {
      userId,
      permissionType
    }
  ]);
  if (error) return;

  window.$message?.success('权限已更新');
  await loadMembers();
  emit('refresh');
}

async function handleRemoveMember(userId: CommonType.IdType) {
  // 检查是否为最后一个管理员
  const adminMembers = members.value.filter(m => m.permissionType === 'admin');
  const targetMember = members.value.find(m => m.userId === userId);

  if (targetMember?.permissionType === 'admin' && adminMembers.length === 1) {
    window.$message?.error('不能移除最后一个管理员');
    return;
  }

  const {error} = await fetchRemoveProjectMembers(props.projectId, [userId]);
  if (error) return;

  window.$message?.success('移除成功');
  await loadMembers();
  emit('refresh');
}

function getPermissionTypeLabel(type: string) {
  const option = permissionTypeOptions.find(opt => opt.value === type);
  return option?.label || type;
}

watch(
  () => props.projectId,
  () => {
    if (props.projectId) {
      loadMembers();
    }
  },
  {immediate: true}
);

</script>

<template>
  <div class="flex-col-stretch gap-16px">
    <NCard v-if="canManage" :bordered="false" size="small" title="邀请成员">
      <NForm label-placement="left" :label-width="100">
        <NGrid item-responsive responsive="screen">
          <NFormItemGi span="24 s:12 m:8" label="权限类型">
            <NSelect
              v-model:value="invitePermissionType"
              :options="permissionTypeOptions"
              placeholder="请选择权限类型"
            />
          </NFormItemGi>
          <NFormItemGi span="24 s:24 m:12" label="邀请链接">
            <NInput v-model:value="inviteLink" readonly placeholder="点击下方按钮生成邀请链接"/>
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:4">
            <div class="flex gap-8px">
              <NButton type="primary" @click="handleGenerateInviteLink">生成邀请链接</NButton>
              <NButton @click="handleCopyInviteLink">复制链接</NButton>
            </div>
          </NFormItemGi>
        </NGrid>
      </NForm>
    </NCard>

    <NCard :bordered="false" size="small" title="成员列表">
      <NSpin :show="loading">
        <NDataTable
          :columns="columns"
          :data="members"
          :row-key="row => row.id"
          size="small"
        />
      </NSpin>
    </NCard>
  </div>
</template>

<style scoped></style>

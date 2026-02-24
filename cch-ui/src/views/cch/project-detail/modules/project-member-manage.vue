<script lang="ts" setup>
import {computed, h, ref, watch} from 'vue';
import type {DataTableColumns} from 'naive-ui';
import {NButton} from 'naive-ui';
import {fetchAddProjectMembers, fetchGetProjectMembers, fetchRemoveProjectMembers} from '@/service/api/cch/project';
import {useAuth} from '@/hooks/business/auth';
import {useAuthStore} from '@/store/modules/auth';
import UserSelect from '@/components/custom/user-select.vue';

defineOptions({
  name: 'ProjectMemberManage'
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
const authStore = useAuthStore();
const currentUserId = computed(() => authStore.userInfo.user?.userId);

const loading = ref(false);
const members = ref<Api.Cch.ProjectMember[]>([]);

const permissionTypeOptions = [
  {label: '管理员', value: 'admin'},
  {label: '查看所有题', value: 'view_all'},
  {label: '仅查看自己导入的题', value: 'view_own'}
];

const addMemberForm = ref({
  userId: null as CommonType.IdType | null,
  permissionType: 'view_all' as 'admin' | 'view_all' | 'view_own'
});

const canManage = computed(() => {
  return props.isProjectAdmin && hasAuth('cch:project:member');
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
      minWidth: 150,
      render: row => getPermissionTypeLabel(row.permissionType)
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

async function handleAddMember() {
  if (!addMemberForm.value.userId) {
    window.$message?.warning('请选择用户');
    return;
  }

  // 检查用户是否已经是成员
  if (members.value.some(m => m.userId === addMemberForm.value.userId)) {
    window.$message?.warning('该用户已经是项目成员');
    return;
  }

  const {error} = await fetchAddProjectMembers(props.projectId, [{
    userId: addMemberForm.value.userId,
    permissionType: addMemberForm.value.permissionType
  }]);

  if (error) return;

  window.$message?.success('添加成功');
  addMemberForm.value.userId = null;
  addMemberForm.value.permissionType = 'view_all';
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

watch(() => props.projectId, () => {
  if (props.projectId) {
    loadMembers();
  }
}, {immediate: true});

</script>

<template>
  <div class="flex-col-stretch gap-16px">
    <NCard v-if="canManage" :bordered="false" size="small" title="添加成员">
      <NForm :model="addMemberForm" label-placement="left" :label-width="100">
        <NGrid item-responsive responsive="screen">
          <NFormItemGi span="24 s:12 m:8" label="选择用户">
            <UserSelect v-model:value="addMemberForm.userId" placeholder="请选择用户"/>
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:8" label="权限类型">
            <NSelect
              v-model:value="addMemberForm.permissionType"
              :options="permissionTypeOptions"
              placeholder="请选择权限类型"
            />
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:8">
            <NButton type="primary" @click="handleAddMember">添加</NButton>
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

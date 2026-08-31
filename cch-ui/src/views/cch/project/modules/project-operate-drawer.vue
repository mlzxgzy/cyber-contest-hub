<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import {jsonClone} from '@sa/utils';
import {fetchCreateProject, fetchUpdateProject} from '@/service/api/cch/project';
import {useFormRules, useNaiveForm} from '@/hooks/common/form';
import {$t} from '@/locales';

defineOptions({
  name: 'ProjectOperateDrawer'
});

interface Props {
  /** the type of operation */
  operateType: NaiveUI.TableOperateType;
  /** the edit row data */
  rowData?: Api.Cch.Project | null;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'submitted'): void;
}

const emit = defineEmits<Emits>();

const visible = defineModel<boolean>('visible', {
  default: false
});

const {formRef, validate, restoreValidation} = useNaiveForm();
const {createRequiredRule} = useFormRules();

const title = computed(() => {
  const titles: Record<NaiveUI.TableOperateType, string> = {
    add: '新增项目',
    edit: '编辑项目'
  };
  return titles[props.operateType];
});

const projectTypeOptions = [
  {label: '普通项目', value: 'normal'},
  {label: '竞赛项目', value: 'contest'}
];

type Model = Api.Cch.ProjectOperateParams;

const model = ref<Model>(createDefaultModel());

function createDefaultModel(): Model {
  return {
    id: null,
    projectType: 'normal',
    name: '',
    remark: '',
    meta: undefined
  };
}

type RuleKey = Extract<
  keyof Model,
  | 'id'
  | 'projectType'
  | 'name'
  | 'remark'
>;

const isContestProject = computed(() => model.value.projectType === 'contest');

const rules = computed<Record<RuleKey, App.Global.FormRule>>(() => ({
  id: createRequiredRule('主键不能为空'),
  projectType: createRequiredRule('项目类型不能为空'),
  name: createRequiredRule(isContestProject.value ? '竞赛名称不能为空' : '项目名称不能为空'),
  remark: createRequiredRule(isContestProject.value ? '赛事备注不能为空' : '备注不能为空')
}));

const nameLabel = computed(() => {
  return isContestProject.value ? '竞赛名称' : '项目名称';
});

const namePlaceholder = computed(() => {
  return isContestProject.value ? '请输入竞赛名称' : '请输入项目名称';
});

const remarkLabel = computed(() => {
  return isContestProject.value ? '赛事备注' : '备注';
});

const remarkPlaceholder = computed(() => {
  return isContestProject.value ? '请输入赛事备注' : '请输入备注';
});

const contestMeta = ref<Api.Cch.ContestMeta>({
  contestName: '',
  contestRemark: '',
  startTime: undefined,
  endTime: undefined,
  challengeRequirement: ''
});

function handleUpdateModelWhenEdit() {
  model.value = createDefaultModel();
  contestMeta.value = {
    contestName: '',
    contestRemark: '',
    startTime: undefined,
    endTime: undefined,
    challengeRequirement: ''
  };

  if (props.operateType === 'edit' && props.rowData) {
    Object.assign(model.value, jsonClone(props.rowData));
    if (props.rowData.meta) {
      // 如果是竞赛项目，将竞赛名称同步到项目名称，将赛事备注同步到备注
      if (model.value.projectType === 'contest') {
        if (props.rowData.meta.contestName) {
          model.value.name = props.rowData.meta.contestName;
        }
        if (props.rowData.meta.contestRemark) {
          model.value.remark = props.rowData.meta.contestRemark;
        }
      }
      contestMeta.value = {
        contestName: props.rowData.meta.contestName || '',
        contestRemark: props.rowData.meta.contestRemark || '',
        startTime: props.rowData.meta.startTime || '',
        endTime: props.rowData.meta.endTime || '',
        challengeRequirement: props.rowData.meta.challengeRequirement || ''
      };
    }
  }
}

function closeDrawer() {
  visible.value = false;
}

async function handleSubmit() {
  await validate();

  const {id, projectType, name, remark} = model.value;

  const submitData: Api.Cch.ProjectOperateParams = {
    id,
    projectType,
    name,
    remark
  };

  // 如果是竞赛项目，添加meta信息
  if (projectType === 'contest') {
    submitData.meta = {
      contestName: name, // 使用项目名称作为竞赛名称
      contestRemark: remark, // 使用备注作为赛事备注
      startTime: contestMeta.value.startTime || '',
      endTime: contestMeta.value.endTime || '',
      challengeRequirement: contestMeta.value.challengeRequirement || ''
    };
  }

  // request
  if (props.operateType === 'add') {
    const {error} = await fetchCreateProject(submitData);
    if (error) return;
  }

  if (props.operateType === 'edit') {
    const {error} = await fetchUpdateProject(submitData);
    if (error) return;
  }

  window.$message?.success($t(props.operateType === 'add' ? 'common.addSuccess' : 'common.updateSuccess'));
  closeDrawer();
  emit('submitted');
}

watch(visible, () => {
  if (visible.value) {
    handleUpdateModelWhenEdit();
    restoreValidation();
  }
});

</script>

<template>
  <NDrawer v-model:show="visible" :title="title" :width="800" class="max-w-90%" display-directive="show">
    <NDrawerContent :native-scrollbar="false" :title="title" closable>
      <NForm ref="formRef" :model="model" :rules="rules">
        <NFormItem label="项目类型" path="projectType">
          <NSelect
            v-model:value="model.projectType"
            :options="projectTypeOptions"
            placeholder="请选择项目类型"
          />
        </NFormItem>
        <NFormItem :label="nameLabel" path="name">
          <NInput v-model:value="model.name" :placeholder="namePlaceholder"/>
        </NFormItem>
        <NFormItem :label="remarkLabel" path="remark">
          <NInput
            v-model:value="model.remark"
            :rows="3"
            :placeholder="remarkPlaceholder"
            type="textarea"
          />
        </NFormItem>
        <div v-if="isContestProject">
          <NDivider>竞赛信息</NDivider>
          <NFormItem label="开始时间">
            <NDatePicker
              v-model:formatted-value="contestMeta.startTime"
              type="date"
              value-format="yyyy-MM-dd"
              clearable
              placeholder="请选择开始时间"
            />
          </NFormItem>
          <NFormItem label="结束时间">
            <NDatePicker
              v-model:formatted-value="contestMeta.endTime"
              type="date"
              value-format="yyyy-MM-dd"
              clearable
              placeholder="请选择结束时间"
            />
          </NFormItem>
          <NFormItem label="题目需求">
            <NInput
              v-model:value="contestMeta.challengeRequirement"
              type="textarea"
              :rows="4"
              placeholder="请输入题目需求描述（支持多行）"
            />
          </NFormItem>
        </div>
      </NForm>
      <template #footer>
        <NSpace :size="16">
          <NButton @click="closeDrawer">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped></style>

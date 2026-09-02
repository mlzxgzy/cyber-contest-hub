<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import {fetchCreateProject} from '@/service/api/cch/project';
import {useFormRules, useNaiveForm} from '@/hooks/common/form';
import {$t} from '@/locales';

defineOptions({
  name: 'ProjectOperateDrawer'
});

const visible = defineModel<boolean>('visible', {
  default: false
});

const emit = defineEmits<{
  (e: 'submitted', id: CommonType.IdType): void;
}>();

const {formRef, validate, restoreValidation} = useNaiveForm();
const {createRequiredRule} = useFormRules();

const projectTypeOptions = [
  {label: '普通项目', value: 'normal'},
  {label: '竞赛项目', value: 'contest'}
];

const model = ref(createDefaultModel());

function createDefaultModel() {
  return {
    projectType: 'normal',
    name: ''
  };
}

const isContestProject = computed(() => model.value.projectType === 'contest');

const nameLabel = computed(() => {
  return isContestProject.value ? '竞赛名称' : '项目名称';
});

const namePlaceholder = computed(() => {
  return isContestProject.value ? '请输入竞赛名称' : '请输入项目名称';
});

const rules = computed(() => ({
  projectType: createRequiredRule('项目类型不能为空'),
  name: createRequiredRule(isContestProject.value ? '竞赛名称不能为空' : '项目名称不能为空')
}));

const submitting = ref(false);

async function handleSubmit() {
  await validate();
  if (submitting.value) return;
  submitting.value = true;

  const {projectType, name} = model.value;
  const {data, error} = await fetchCreateProject({projectType, name});
  submitting.value = false;

  if (error || !data) return;

  window.$message?.success($t('common.addSuccess'));
  visible.value = false;
  emit('submitted', data);
}

watch(visible, () => {
  if (visible.value) {
    model.value = createDefaultModel();
    restoreValidation();
  }
});
</script>

<template>
  <NDrawer v-model:show="visible" title="新增项目" :width="440" class="max-w-90%">
    <NDrawerContent :native-scrollbar="false" title="新增项目" closable>
      <NForm ref="formRef" :model="model" :rules="rules" label-placement="left" label-width="80">
        <NFormItem label="项目类型" path="projectType">
          <NRadioGroup v-model:value="model.projectType">
            <NSpace>
              <NRadio v-for="opt in projectTypeOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </NRadio>
            </NSpace>
          </NRadioGroup>
        </NFormItem>
        <NFormItem :label="nameLabel" path="name">
          <NInput
            v-model:value="model.name"
            :placeholder="namePlaceholder"
            :maxlength="128"
            show-count
            @keyup.enter="handleSubmit"
          />
        </NFormItem>
      </NForm>
      <p class="text-12px text-gray-400">项目创建后可在详情页补充备注、竞赛信息等内容</p>
      <template #footer>
        <NSpace :size="16">
          <NButton @click="visible = false">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" :loading="submitting" @click="handleSubmit">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

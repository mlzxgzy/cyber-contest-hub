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
  {label: '竞赛项目', value: 'contest'},
  {label: '出题项目', value: 'authoring'}
];

const authorSourceOptions = [
  {label: '自己出', value: 'self'},
  {label: '外采', value: 'external'}
];

const model = ref(createDefaultModel());

function createDefaultModel() {
  return {
    projectType: 'normal' as 'normal' | 'contest' | 'authoring',
    name: '',
    authoringMeta: {
      authorSource: 'self' as 'self' | 'external',
      externalUnit: ''
    }
  };
}

const isContestProject = computed(() => model.value.projectType === 'contest');
const isAuthoringProject = computed(() => model.value.projectType === 'authoring');

const nameLabel = computed(() => {
  return isContestProject.value ? '竞赛名称' : '项目名称';
});

const namePlaceholder = computed(() => {
  return isContestProject.value ? '请输入竞赛名称' : '请输入项目名称';
});

const rules = computed(() => {
  const base: Record<string, App.Global.FormRule> = {
    projectType: createRequiredRule('项目类型不能为空'),
    name: createRequiredRule(isContestProject.value ? '竞赛名称不能为空' : '项目名称不能为空')
  };
  if (isAuthoringProject.value) {
    base['authoringMeta.authorSource'] = createRequiredRule('出题方式不能为空');
  }
  return base;
});

const submitting = ref(false);

async function handleSubmit() {
  await validate();
  if (submitting.value) return;
  submitting.value = true;

  const {projectType, name, authoringMeta} = model.value;
  const {data, error} = await fetchCreateProject({
    projectType,
    name,
    authoringMeta:
      projectType === 'authoring'
        ? {
            authorSource: authoringMeta.authorSource,
            externalUnit: authoringMeta.authorSource === 'external' ? authoringMeta.externalUnit || undefined : undefined
          }
        : undefined
  });
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
        <NFormItem v-if="isAuthoringProject" label="出题方式" path="authoringMeta.authorSource">
          <NRadioGroup v-model:value="model.authoringMeta.authorSource">
            <NSpace>
              <NRadio v-for="opt in authorSourceOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </NRadio>
            </NSpace>
          </NRadioGroup>
        </NFormItem>
        <NFormItem
          v-if="isAuthoringProject && model.authoringMeta.authorSource === 'external'"
          label="外采单位"
          path="authoringMeta.externalUnit"
        >
          <NInput
            v-model:value="model.authoringMeta.externalUnit"
            placeholder="请输入外采单位名称（可留空）"
            :maxlength="128"
          />
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

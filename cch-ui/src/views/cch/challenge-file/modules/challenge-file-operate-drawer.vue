<script setup lang="ts">
import {computed, ref, watch} from 'vue';
import {jsonClone} from '@sa/utils';
import {fetchCreateChallengeFile, fetchUpdateChallengeFile} from '@/service/api/cch/challenge-file';
import {useFormRules, useNaiveForm} from '@/hooks/common/form';
import {$t} from '@/locales';

defineOptions({
  name: 'ChallengeFileOperateDrawer'
});

interface Props {
  /** the type of operation */
  operateType: NaiveUI.TableOperateType;
  /** the edit row data */
  rowData?: Api.Cch.ChallengeFile | null;
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
    add: '新增题目文件',
    edit: '编辑题目文件'
  };
  return titles[props.operateType];
});

type Model = Api.Cch.ChallengeFileOperateParams;

const model = ref<Model>(createDefaultModel());

function createDefaultModel(): Model {
  return {
    id: null,
    challengeId: null,
    fileName: '',
    originalName: '',
    fileSuffix: '',
    url: '',
    ext1: '',
    service: ''
  };
}

type RuleKey = Extract<
    keyof Model,
    | 'id'
    | 'challengeId'
    | 'fileName'
    | 'originalName'
    | 'fileSuffix'
    | 'url'
    | 'service'
>;

const rules: Record<RuleKey, App.Global.FormRule> = {
  id: createRequiredRule('主键不能为空'),
  challengeId: createRequiredRule('题目id不能为空'),
  fileName: createRequiredRule('文件名不能为空'),
  originalName: createRequiredRule('原名不能为空'),
  fileSuffix: createRequiredRule('文件后缀名不能为空'),
  url: createRequiredRule('URL地址不能为空'),
  service: createRequiredRule('服务商不能为空')
};

function handleUpdateModelWhenEdit() {
  model.value = createDefaultModel();

  if (props.operateType === 'edit' && props.rowData) {
    Object.assign(model.value, jsonClone(props.rowData));
  }
}

function closeDrawer() {
  visible.value = false;
}

async function handleSubmit() {
  await validate();

  const {id, challengeId, fileName, originalName, fileSuffix, url, ext1, service} = model.value;

  // request
  if (props.operateType === 'add') {
    const {error} = await fetchCreateChallengeFile({challengeId, fileName, originalName, fileSuffix, url, ext1, service});
    if (error) return;
  }

  if (props.operateType === 'edit') {
    const {error} = await fetchUpdateChallengeFile({id, challengeId, fileName, originalName, fileSuffix, url, ext1, service});
    if (error) return;
  }

  window.$message?.success($t('common.updateSuccess'));
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
  <NDrawer v-model:show="visible" :title="title" display-directive="show" :width="800" class="max-w-90%">
    <NDrawerContent :title="title" :native-scrollbar="false" closable>
      <NForm ref="formRef" :model="model" :rules="rules">
        <NFormItem label="题目id" path="challengeId">
          <NInput v-model:value="model.challengeId" placeholder="请输入题目id"/>
        </NFormItem>
        <NFormItem label="文件名" path="fileName">
          <NInput v-model:value="model.fileName" placeholder="请输入文件名"/>
        </NFormItem>
        <NFormItem label="原名" path="originalName">
          <NInput v-model:value="model.originalName" placeholder="请输入原名"/>
        </NFormItem>
        <NFormItem label="文件后缀名" path="fileSuffix">
          <NInput v-model:value="model.fileSuffix" placeholder="请输入文件后缀名"/>
        </NFormItem>
        <NFormItem label="URL地址" path="url">
          <NInput
              v-model:value="model.url"
              :rows="3"
              type="textarea"
              placeholder="请输入URL地址"
          />
        </NFormItem>
        <NFormItem label="扩展字段" path="ext1">
          <NInput
              v-model:value="model.ext1"
              :rows="3"
              type="textarea"
              placeholder="请输入扩展字段"
          />
        </NFormItem>
        <NFormItem label="服务商" path="service">
          <NInput v-model:value="model.service" placeholder="请输入服务商"/>
        </NFormItem>
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

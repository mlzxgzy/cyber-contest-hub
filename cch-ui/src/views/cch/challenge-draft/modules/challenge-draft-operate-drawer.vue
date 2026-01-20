<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import {jsonClone} from '@sa/utils';
import {fetchCreateChallengeDraft, fetchUpdateChallengeDraft, fetchEditChallengeDraft} from '@/service/api/cch/challenge-draft';
import {useFormRules, useNaiveForm} from '@/hooks/common/form';
import {$t} from '@/locales';

defineOptions({
  name: 'ChallengeDraftOperateDrawer'
});

interface Props {
  /** the type of operation */
  operateType: NaiveUI.TableOperateType;
  /** the edit row data */
  rowData?: Api.Cch.ChallengeDraft | null;
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
    add: '新增题目草稿',
    edit: '编辑题目草稿'
  };
  return titles[props.operateType];
});

type Model = Api.Cch.ChallengeDraftOperateParams;

const model = ref<Model>(createDefaultModel());

function createDefaultModel(): Model {
  return {
    id: null,
    challengeId: null,
    challengeName: '',
    challengeDescription: '',
    config: '',
  };
}

type RuleKey = Extract<
  keyof Model,
  | 'id'
  | 'challengeId'
  | 'challengeName'
  | 'config'
  | 'createTime'
  | 'updateTime'
>;

const rules: Record<RuleKey, App.Global.FormRule> = {
  id: createRequiredRule('主键不能为空'),
  challengeId: createRequiredRule('题目ID不能为空'),
  challengeName: createRequiredRule('题目名称不能为空'),
  config: createRequiredRule('配置不能为空'),
  createTime: createRequiredRule('创建时间不能为空'),
  updateTime: createRequiredRule('更新时间不能为空'),
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

  const {id, challengeId, challengeName, challengeDescription, config} = model.value;

  // request
  if (props.operateType === 'add') {
    const {error} = await fetchCreateChallengeDraft({challengeId, challengeName, challengeDescription, config});
    if (error) return;
  }

  if (props.operateType === 'edit') {
    // 编辑模式：直接更新现有草稿，不新增版本
    const {error} = await fetchEditChallengeDraft({id, challengeId, challengeName, challengeDescription, config});
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
  <NDrawer v-model:show="visible" :title="title" :width="800" class="max-w-90%" display-directive="show">
    <NDrawerContent :native-scrollbar="false" :title="title" closable>
      <NForm ref="formRef" :model="model" :rules="rules">
        <NFormItem label="题目ID" path="challengeId">
          <NInput v-model:value="model.challengeId" placeholder="请输入题目ID"/>
        </NFormItem>
        <NFormItem label="题目名称" path="challengeName">
          <NInput v-model:value="model.challengeName" placeholder="请输入题目名称"/>
        </NFormItem>
        <NFormItem label="草稿描述" path="challengeDescription">
          <NInput
            v-model:value="model.challengeDescription"
            :rows="3"
            placeholder="请输入草稿描述"
            type="textarea"
          />
        </NFormItem>
        <NFormItem label="配置" path="config">
          <NInput
            v-model:value="model.config"
            :rows="3"
            placeholder="请输入配置"
            type="textarea"
          />
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

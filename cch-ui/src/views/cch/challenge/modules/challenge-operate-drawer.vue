<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import {jsonClone} from '@sa/utils';
import {fetchCreateChallenge, fetchUpdateChallenge} from '@/service/api/cch/challenge';
import {useFormRules, useNaiveForm} from '@/hooks/common/form';
import {useDict} from '@/hooks/business/dict';
import {$t} from '@/locales';

defineOptions({
  name: 'ChallengeOperateDrawer'
});

interface Props {
  /** the type of operation */
  operateType: NaiveUI.TableOperateType;
  /** the edit row data */
  rowData?: Api.Cch.Challenge | null;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'submitted'): void;
}

const emit = defineEmits<Emits>();

const visible = defineModel<boolean>('visible', {
  default: false
});

const {options: cchQuestionCategroyOptions} = useDict('cch_question_categroy');

const {formRef, validate, restoreValidation} = useNaiveForm();
const {createRequiredRule} = useFormRules();

const title = computed(() => {
  const titles: Record<NaiveUI.TableOperateType, string> = {
    add: '新增题目列表',
    edit: '编辑题目列表'
  };
  return titles[props.operateType];
});

type Model = Api.Cch.ChallengeOperateParams;

const model = ref<Model>(createDefaultModel());

function createDefaultModel(): Model {
  return {
    id: null,
    category: '',
    name: '',
    description: '',
    latestVersionId: null,
  };
}

type RuleKey = Extract<
  keyof Model,
  | 'id'
  | 'category'
  | 'name'
  | 'description'
  | 'createTime'
  | 'updateTime'
>;

const rules: Record<RuleKey, App.Global.FormRule> = {
  id: createRequiredRule('主键不能为空'),
  category: createRequiredRule('题目类型不能为空'),
  name: createRequiredRule('题目名称不能为空'),
  description: createRequiredRule('题目描述不能为空'),
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

  const {id, category, name, description} = model.value;

  // request
  if (props.operateType === 'add') {
    const {error} = await fetchCreateChallenge({category, name, description});
    if (error) return;
  }

  if (props.operateType === 'edit') {
    const {error} = await fetchUpdateChallenge({id, category, name, description});
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
        <NFormItem label="题目类型" path="category">
          <NSelect
            v-model:value="model.category"
            :options="cchQuestionCategroyOptions"
            clearable
            placeholder="请选择题目类型"
          />
        </NFormItem>
        <NFormItem label="题目名称" path="name">
          <NInput v-model:value="model.name" placeholder="请输入题目名称"/>
        </NFormItem>
        <NFormItem label="题目描述" path="description">
          <NInput
            v-model:value="model.description"
            :rows="3"
            placeholder="请输入题目描述"
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

<script lang="ts" setup>
import {computed, ref, watch} from 'vue';
import {jsonClone} from '@sa/utils';
import {fetchCreateChallengeVersion, fetchUpdateChallengeVersion} from '@/service/api/cch/challenge-version';
import {useFormRules, useNaiveForm} from '@/hooks/common/form';
import {$t} from '@/locales';

defineOptions({
  name: 'ChallengeVersionOperateDrawer'
});

interface Props {
  /** the type of operation */
  operateType: NaiveUI.TableOperateType;
  /** the edit row data */
  rowData?: Api.Cch.ChallengeVersion | null;
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
    add: '新增题目版本',
    edit: '编辑题目版本'
  };
  return titles[props.operateType];
});

type Model = Api.Cch.ChallengeVersionOperateParams;

const model = ref<Model>(createDefaultModel());

function createDefaultModel(): Model {
  return {
    id: null,
    challengeId: null,
    challengeName: '',
    draftId: null,
    versionTag: '',
    versionDescription: '',
  };
}

type RuleKey = Extract<
  keyof Model,
  | 'id'
  | 'challengeId'
  | 'challengeName'
  | 'draftId'
  | 'versionTag'
  | 'createTime'
  | 'updateTime'
>;

const rules: Record<RuleKey, App.Global.FormRule> = {
  id: createRequiredRule('主键不能为空'),
  challengeId: createRequiredRule('题目ID不能为空'),
  challengeName: createRequiredRule('题目名称不能为空'),
  draftId: createRequiredRule('草稿ID不能为空'),
  versionTag: createRequiredRule('版本号不能为空'),
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

  const {id, challengeId, challengeName, draftId, versionTag, versionDescription} = model.value;

  // request
  if (props.operateType === 'add') {
    const {error} = await fetchCreateChallengeVersion({
      challengeId,
      challengeName,
      draftId,
      versionTag,
      versionDescription
    });
    if (error) return;
  }

  if (props.operateType === 'edit') {
    const {error} = await fetchUpdateChallengeVersion({
      id,
      challengeId,
      challengeName,
      draftId,
      versionTag,
      versionDescription
    });
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
        <NFormItem label="草稿ID" path="draftId">
          <NInput v-model:value="model.draftId" placeholder="请输入草稿ID"/>
        </NFormItem>
        <NFormItem label="版本号" path="versionTag">
          <NInput v-model:value="model.versionTag" placeholder="请输入版本号"/>
        </NFormItem>
        <NFormItem label="版本描述" path="versionDescription">
          <NInput
            v-model:value="model.versionDescription"
            :rows="3"
            placeholder="请输入版本描述"
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

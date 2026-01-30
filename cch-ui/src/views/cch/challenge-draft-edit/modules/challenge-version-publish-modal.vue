<script lang="ts" setup>
import { ref, watch } from 'vue';
import {
  NModal,
  NForm,
  NFormItem,
  NInput,
  NButton,
  NSpace
} from 'naive-ui';
import { fetchCreateChallengeVersion } from '@/service/api/cch/challenge-version';
import { useFormRules, useNaiveForm } from '@/hooks/common/form';

defineOptions({
  name: 'ChallengeVersionPublishModal'
});

interface Props {
  challengeId: CommonType.IdType | null;
  challengeName: string;
  draftId: CommonType.IdType | null;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'submitted'): void;
}

const emit = defineEmits<Emits>();

const visible = defineModel<boolean>('visible', {
  default: false
});

const { formRef, validate, restoreValidation } = useNaiveForm();
const { createRequiredRule } = useFormRules();

type Model = {
  challengeId: CommonType.IdType | null;
  challengeName: string;
  draftId: CommonType.IdType | null;
  versionTag: string;
  versionDescription: string;
};

const model = ref<Model>({
  challengeId: null,
  challengeName: '',
  draftId: null,
  versionTag: '',
  versionDescription: ''
});

const rules = {
  versionTag: createRequiredRule('版本号不能为空')
};

function resetModel() {
  model.value = {
    challengeId: props.challengeId,
    challengeName: props.challengeName,
    draftId: props.draftId,
    versionTag: '',
    versionDescription: ''
  };
}

function closeModal() {
  visible.value = false;
}

async function handleSubmit() {
  await validate();

  const { challengeId, challengeName, draftId, versionTag, versionDescription } = model.value;

  if (!challengeId || !draftId) {
    window.$message?.error('题目ID或草稿ID不能为空');
    return;
  }

  const { error } = await fetchCreateChallengeVersion({
    challengeId,
    challengeName,
    draftId,
    versionTag,
    versionDescription
  });

  if (error) {
    window.$message?.error(`发版失败: ${error}`);
    return;
  }

  window.$message?.success('发版成功');
  closeModal();
  emit('submitted');
}

watch(visible, () => {
  if (visible.value) {
    resetModel();
    restoreValidation();
  }
});

watch(
  () => [props.challengeId, props.challengeName, props.draftId],
  () => {
    if (visible.value) {
      resetModel();
    }
  }
);
</script>

<template>
  <NModal
    v-model:show="visible"
    preset="card"
    title="发版"
    :bordered="false"
    display-directive="show"
    class="max-w-90% w-600px"
    @close="closeModal"
  >
    <NForm ref="formRef" :model="model" :rules="rules" label-placement="left" label-width="100px">
      <NFormItem label="题目名称" path="challengeName">
        <NInput v-model:value="model.challengeName" disabled placeholder="题目名称" />
      </NFormItem>
      <NFormItem label="版本号" path="versionTag">
        <NInput
          v-model:value="model.versionTag"
          placeholder="请输入版本号，例如：v1.0.0"
          clearable
        />
      </NFormItem>
      <NFormItem label="版本描述" path="versionDescription">
        <NInput
          v-model:value="model.versionDescription"
          :rows="4"
          placeholder="请输入版本描述"
          type="textarea"
        />
      </NFormItem>
    </NForm>
    <template #footer>
      <NSpace justify="end" :size="16">
        <NButton @click="closeModal">取消</NButton>
        <NButton type="primary" @click="handleSubmit">确认发版</NButton>
      </NSpace>
    </template>
  </NModal>
</template>

<style scoped lang="scss">
:deep(.n-form-item-label) {
  font-weight: 500;
  color: #1f2937;
}
</style>

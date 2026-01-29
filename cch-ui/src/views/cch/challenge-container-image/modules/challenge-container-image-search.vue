<script lang="ts" setup>
import {toRaw} from 'vue';
import {jsonClone} from '@sa/utils';
import {useNaiveForm} from '@/hooks/common/form';
import {$t} from '@/locales';

defineOptions({
  name: 'ChallengeContainerImageSearch'
});

interface Emits {
  (e: 'search'): void;
}

const emit = defineEmits<Emits>();

const {formRef, validate, restoreValidation} = useNaiveForm();

const model = defineModel<Api.Cch.ChallengeContainerImageSearchParams>('model', {required: true});

// 状态选项
const statusOptions = [
  {label: '上传中', value: 'uploading'},
  {label: '已上传', value: 'uploaded'},
  {label: '验证中', value: 'validating'},
  {label: '可用', value: 'available'},
  {label: '错误', value: 'error'}
];

const defaultModel = jsonClone(toRaw(model.value));

function resetModel() {
  Object.assign(model.value, defaultModel);
}

async function reset() {
  await restoreValidation();
  resetModel();
  emit('search');
}

async function search() {
  await validate();
  emit('search');
}
</script>

<template>
  <NCard :bordered="false" class="card-wrapper" size="small">
    <NCollapse>
      <NCollapseItem :title="$t('common.search')" name="cch-challenge-container-image-search">
        <NForm ref="formRef" :label-width="80" :model="model" label-placement="left">
          <NGrid item-responsive responsive="screen">
            <NFormItemGi class="pr-24px" label="题目ID" label-width="auto" path="challengeId" span="24 s:12 m:6">
              <NInputNumber v-model:value="model.challengeId" :precision="0" :min="1" placeholder="请输入题目ID" style="width: 100%"/>
            </NFormItemGi>
            <NFormItemGi class="pr-24px" label="镜像名称" label-width="auto" path="imageName" span="24 s:12 m:6">
              <NInput v-model:value="model.imageName" placeholder="请输入镜像名称"/>
            </NFormItemGi>
            <NFormItemGi class="pr-24px" label="状态" label-width="auto" path="status" span="24 s:12 m:6">
              <NSelect
                v-model:value="model.status"
                :options="statusOptions"
                clearable
                placeholder="请选择状态"
              />
            </NFormItemGi>
            <NFormItemGi :show-feedback="false" class="pr-24px" span="24">
              <NSpace class="w-full" justify="end">
                <NButton @click="reset">
                  <template #icon>
                    <icon-ic-round-refresh class="text-icon"/>
                  </template>
                  {{ $t('common.reset') }}
                </NButton>
                <NButton ghost type="primary" @click="search">
                  <template #icon>
                    <icon-ic-round-search class="text-icon"/>
                  </template>
                  {{ $t('common.search') }}
                </NButton>
              </NSpace>
            </NFormItemGi>
          </NGrid>
        </NForm>
      </NCollapseItem>
    </NCollapse>
  </NCard>
</template>

<style scoped></style>

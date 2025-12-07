<script setup lang="ts">
import {toRaw} from 'vue';
import {jsonClone} from '@sa/utils';
import {useNaiveForm} from '@/hooks/common/form';
import {$t} from '@/locales';

defineOptions({
  name: 'ChallengeFileSearch'
});

interface Emits {
  (e: 'search'): void;
}

const emit = defineEmits<Emits>();

const {formRef, validate, restoreValidation} = useNaiveForm();

const model = defineModel<Api.Cch.ChallengeFileSearchParams>('model', {required: true});

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
  <NCard :bordered="false" size="small" class="card-wrapper">
    <NCollapse>
      <NCollapseItem :title="$t('common.search')" name="cch-challenge-file-search">
        <NForm ref="formRef" :model="model" label-placement="left" :label-width="80">
          <NGrid responsive="screen" item-responsive>
            <NFormItemGi span="24 s:12 m:6" label="题目id" label-width="auto" path="challengeId" class="pr-24px">
              <NInput v-model:value="model.challengeId" placeholder="请输入题目id"/>
            </NFormItemGi>
            <NFormItemGi span="24 s:12 m:6" label="文件名" label-width="auto" path="fileName" class="pr-24px">
              <NInput v-model:value="model.fileName" placeholder="请输入文件名"/>
            </NFormItemGi>
            <NFormItemGi span="24 s:12 m:6" label="原名" label-width="auto" path="originalName" class="pr-24px">
              <NInput v-model:value="model.originalName" placeholder="请输入原名"/>
            </NFormItemGi>
            <NFormItemGi span="24 s:12 m:6" label="文件后缀名" label-width="auto" path="fileSuffix" class="pr-24px">
              <NInput v-model:value="model.fileSuffix" placeholder="请输入文件后缀名"/>
            </NFormItemGi>
            <NFormItemGi span="24 s:12 m:6" label="URL地址" label-width="auto" path="url" class="pr-24px">
              <NInput v-model:value="model.url" placeholder="请输入URL地址"/>
            </NFormItemGi>
            <NFormItemGi span="24 s:12 m:6" label="服务商" label-width="auto" path="service" class="pr-24px">
              <NInput v-model:value="model.service" placeholder="请输入服务商"/>
            </NFormItemGi>
            <NFormItemGi :show-feedback="false" span="24" class="pr-24px">
              <NSpace class="w-full" justify="end">
                <NButton @click="reset">
                  <template #icon>
                    <icon-ic-round-refresh class="text-icon"/>
                  </template>
                  {{ $t('common.reset') }}
                </NButton>
                <NButton type="primary" ghost @click="search">
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

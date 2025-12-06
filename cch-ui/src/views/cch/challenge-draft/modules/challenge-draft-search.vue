<script lang="ts" setup>
import {toRaw} from 'vue';
import {jsonClone} from '@sa/utils';
import {useNaiveForm} from '@/hooks/common/form';
import {$t} from '@/locales';

defineOptions({
  name: 'ChallengeDraftSearch'
});

interface Emits {
  (e: 'search'): void;
}

const emit = defineEmits<Emits>();

const {formRef, validate, restoreValidation} = useNaiveForm();

const model = defineModel<Api.Cch.ChallengeDraftSearchParams>('model', {required: true});

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
      <NCollapseItem :title="$t('common.search')" name="cch-challenge-draft-search">
        <NForm ref="formRef" :label-width="80" :model="model" label-placement="left">
          <NGrid item-responsive responsive="screen">
            <NFormItemGi class="pr-24px" label="题目ID" label-width="auto" path="challengeId" span="24 s:12 m:6">
              <NInput v-model:value="model.challengeId" placeholder="请输入题目ID"/>
            </NFormItemGi>
            <NFormItemGi class="pr-24px" label="题目名称" label-width="auto" path="challengeName" span="24 s:12 m:6">
              <NInput v-model:value="model.challengeName" placeholder="请输入题目名称"/>
            </NFormItemGi>
            <NFormItemGi class="pr-24px" label="草稿描述" label-width="auto" path="challengeDescription"
                         span="24 s:12 m:6">
              <NInput v-model:value="model.challengeDescription" placeholder="请输入草稿描述"/>
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

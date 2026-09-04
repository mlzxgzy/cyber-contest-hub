<script lang="ts" setup>
import {toRaw} from 'vue';
import {jsonClone} from '@sa/utils';
import {useNaiveForm} from '@/hooks/common/form';
import {$t} from '@/locales';

defineOptions({
  name: 'ProjectSearch'
});

interface Emits {
  (e: 'search'): void;
}

const emit = defineEmits<Emits>();

const {formRef, validate, restoreValidation} = useNaiveForm();

const model = defineModel<Api.Cch.ProjectSearchParams>('model', {required: true});

const defaultModel = jsonClone(toRaw(model.value));

const projectTypeOptions = [
  {label: '普通项目', value: 'normal'},
  {label: '竞赛项目', value: 'contest'},
  {label: '出题项目', value: 'authoring'}
];

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
      <NCollapseItem :title="$t('common.search')" name="cch-project-search">
        <NForm ref="formRef" :label-width="80" :model="model" label-placement="left">
          <NGrid item-responsive responsive="screen">
            <NFormItemGi class="pr-24px" label="项目类型" label-width="auto" path="projectType" span="24 s:12 m:6">
              <NSelect
                v-model:value="model.projectType"
                :options="projectTypeOptions"
                clearable
                placeholder="请选择项目类型"
              />
            </NFormItemGi>
            <NFormItemGi class="pr-24px" label="项目名称" label-width="auto" path="name" span="24 s:12 m:6">
              <NInput v-model:value="model.name" placeholder="请输入项目名称"/>
            </NFormItemGi>
            <NFormItemGi class="pr-24px" label="备注" label-width="auto" path="remark" span="24 s:12 m:6">
              <NInput v-model:value="model.remark" placeholder="请输入备注"/>
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

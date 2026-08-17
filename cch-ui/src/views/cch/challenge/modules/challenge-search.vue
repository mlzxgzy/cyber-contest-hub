<script lang="ts" setup>
import {onMounted, ref, toRaw, watch} from 'vue';
import {jsonClone} from '@sa/utils';
import {fetchGetChallengeKnowledgeTags} from '@/service/api/cch/challenge';
import {useNaiveForm} from '@/hooks/common/form';
import {useDict} from '@/hooks/business/dict';
import {$t} from '@/locales';

defineOptions({
  name: 'ChallengeSearch'
});

interface Emits {
  (e: 'search'): void;
}

const emit = defineEmits<Emits>();

const {formRef, validate, restoreValidation} = useNaiveForm();

const model = defineModel<Api.Cch.ChallengeSearchParams>('model', {required: true});
const {options: cchQuestionCategroyOptions} = useDict('cch_question_categroy');
const {options: cchQuestionDifficultyOptions} = useDict('cch_question_difficulty');

const defaultModel = jsonClone(toRaw(model.value));

// 知识点标签选项（由后端定时维护的缓存提供）
const knowledgeOptions = ref<CommonType.Option[]>([]);
const knowledgeLoading = ref(false);

async function loadKnowledgeOptions() {
  knowledgeLoading.value = true;
  try {
    const {data, error} = await fetchGetChallengeKnowledgeTags();
    if (error) {
      window.$message?.error(`获取知识点列表失败: ${error}`);
      return;
    }
    knowledgeOptions.value = (data || []).map(tag => ({label: tag, value: tag}));
  } finally {
    knowledgeLoading.value = false;
  }
}

onMounted(loadKnowledgeOptions);

// NSelect 不支持 boolean 值，用字符串中转（'1'=已入库，'0'=草稿中）
const publishedSelect = ref<'1' | '0' | null>(null);
watch(publishedSelect, val => {
  if (val === '1') {
    model.value.published = true;
  } else if (val === '0') {
    model.value.published = false;
  } else {
    model.value.published = null;
  }
});
watch(
  () => model.value.published,
  val => {
    if (val === true) {
      publishedSelect.value = '1';
    } else if (val === false) {
      publishedSelect.value = '0';
    } else {
      publishedSelect.value = null;
    }
  }
);

function resetModel() {
  Object.assign(model.value, defaultModel);
  publishedSelect.value = null;
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
      <NCollapseItem :title="$t('common.search')" name="cch-challenge-search">
        <NForm ref="formRef" :label-width="80" :model="model" label-placement="left">
          <NGrid item-responsive responsive="screen">
            <NFormItemGi class="pr-24px" label="题目类型" label-width="auto" path="category" span="24 s:12 m:6">
              <NSelect
                v-model:value="model.category"
                :options="cchQuestionCategroyOptions"
                clearable
                placeholder="请选择题目类型"
              />
            </NFormItemGi>
            <NFormItemGi class="pr-24px" label="题目名称" label-width="auto" path="name" span="24 s:12 m:6">
              <NInput v-model:value="model.name" placeholder="请输入题目名称"/>
            </NFormItemGi>
            <NFormItemGi class="pr-24px" label="题目备注" label-width="auto" path="remark" span="24 s:12 m:6">
              <NInput v-model:value="model.remark" placeholder="请输入题目备注"/>
            </NFormItemGi>
            <NFormItemGi class="pr-24px" label="难度" label-width="auto" path="difficulty" span="24 s:12 m:6">
              <NSelect
                v-model:value="model.difficulty"
                :options="cchQuestionDifficultyOptions"
                clearable
                placeholder="请选择难度"
              />
            </NFormItemGi>
            <NFormItemGi class="pr-24px" label="知识点" label-width="auto" path="knowledge" span="24 s:12 m:6">
              <NSelect
                v-model:value="model.knowledge"
                :options="knowledgeOptions"
                :loading="knowledgeLoading"
                filterable
                clearable
                placeholder="请选择知识点"
              />
            </NFormItemGi>
            <NFormItemGi class="pr-24px" label="入库状态" label-width="auto" path="published" span="24 s:12 m:6">
              <NSelect
                v-model:value="publishedSelect"
                :options="[
                  {label: '已入库', value: '1'},
                  {label: '草稿中', value: '0'}
                ]"
                clearable
                placeholder="全部"
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

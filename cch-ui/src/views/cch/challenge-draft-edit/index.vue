<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {NButton, NCard, NInput, NSpace} from 'naive-ui';
import {fetchChallengeDraftByChallengeId, fetchGetChallengeById} from '@/service/api/cch/challenge';
import {fetchUpdateChallengeDraft} from '@/service/api/cch/challenge-draft';
import {useDict} from "@/hooks/business/dict";
import {useFormRules} from "@/hooks/common/form";

defineOptions({
  name: 'ChallengeDraftEdit'
});

const route = useRoute();
const router = useRouter();

const challengeId = ref<CommonType.IdType | null>(null);
const challengeData = ref<Api.Cch.Challenge | null>(null);
const draftData = ref<Api.Cch.ChallengeDraft | null>(null);
const loading = ref(true);
const saving = ref(false);

const {createRequiredRule} = useFormRules();

const {options: cchQuestionCategroyOptions} = useDict('cch_question_categroy');
const {options: cchQuestionDifficultyOptions} = useDict('cch_question_difficulty');

type challengeModel = Api.Cch.ChallengeOperateParams;
type challengeRuleKey = Extract<
    keyof challengeModel,
    | 'id'
    | 'category'
    | 'name'
    | 'description'
    | 'createTime'
    | 'updateTime'
>;
const challengeRules: Record<challengeRuleKey, App.Global.FormRule> = {
  id: createRequiredRule('主键不能为空'),
  category: createRequiredRule('题目类型不能为空'),
  name: createRequiredRule('题目名称不能为空'),
  createTime: createRequiredRule('创建时间不能为空'),
  updateTime: createRequiredRule('更新时间不能为空'),
};
const draftRules: Record<string, App.Global.FormRule> = {};

onMounted(async () => {
  // 从查询参数中获取 challengeId
  const id = route.query.challengeId;
  if (id) {
    challengeId.value = id;
    // 获取草稿数据
    await loadChallengeData(challengeId.value);
    await loadDraftData(challengeId.value);
  } else {
    window.$message?.error('缺少必要的参数');
    router.back();
  }
});

async function loadChallengeData(id: CommonType.IdType) {
  const {data, error} = await fetchGetChallengeById(id);
  if (error) {
    window.$message?.error('获取题目数据失败: ' + error);
    return;
  }
  challengeData.value = data;
  console.log('获取到的题目数据:', data);
}

async function loadDraftData(id: CommonType.IdType) {
  const {data, error} = await fetchChallengeDraftByChallengeId(id);
  if (error) {
    window.$message?.error('获取草稿数据失败: ' + error);
    return;
  }
  draftData.value = data;
  console.log('获取到的草稿数据:', data);
  loading.value = false;
}

async function saveDraft() {
  if (!draftData.value) return;

  saving.value = true;
  try {
    const {error} = await fetchUpdateChallengeDraft({
      id: draftData.value.id,
      challengeId: draftData.value.challengeId,
      challengeName: draftData.value.challengeName,
      challengeDescription: draftData.value.challengeDescription,
      config: draftData.value.config
    });

    if (error) {
      window.$message?.error('保存失败: ' + error);
      return;
    }

    window.$message?.success('保存成功');
  } catch (err) {
    window.$message?.error('保存异常: ' + err);
  } finally {
    saving.value = false;
  }
}

function goBack() {
  router.back();
}

console.log(draftData)
</script>

<template>
  <div class="p-20px">
    <n-card title="题目草稿编辑">
      <template #header-extra>
        <NSpace>
          <NButton :loading="saving" type="primary" @click="saveDraft">保存</NButton>
          <NButton @click="goBack">返回</NButton>
        </NSpace>
      </template>
      <div v-if="loading">加载中...</div>
      <div v-else-if="draftData">
        <n-tabs animated default-value="info" type="segment">
          <n-tab-pane name="info" tab="题目信息">
            <n-grid cols="1 600:2 1200:3" x-gap="12">
              <n-gi>
                <n-card :segmented="{content: true}" title="基本信息">
                  <NForm ref="challengeFormRef" :model="challengeData" :rules="challengeRules">
                    <NFormItem label="题目类型" path="category">
                      <NSelect
                          v-model:value="challengeData.category"
                          :options="cchQuestionCategroyOptions"
                          clearable
                          placeholder="请选择题目类型"
                      />
                    </NFormItem>
                    <NFormItem label="题目名称" path="name">
                      <NInput v-model:value="challengeData.name" placeholder="请输入题目名称"/>
                    </NFormItem>
                    <NFormItem label="题目备注" path="remark">
                      <NInput
                          v-model:value="challengeData.remark"
                          :rows="3"
                          placeholder="请输入题目备注"
                          type="textarea"
                      />
                    </NFormItem>
                  </NForm>
                </n-card>
              </n-gi>
              <n-gi>
                <n-card :segmented="{content: true}" title="题目信息">
                  <NForm ref="draftFormRef" :model="draftData.config" :rules="draftRules">
                    <NFormItem label="难度" path="difficulty">
                      <NSelect
                          v-model:value="draftData.config.difficulty"
                          :options="cchQuestionDifficultyOptions"
                          clearable
                          placeholder="请选择题目难度"
                      />
                    </NFormItem>
                    <NFormItem label="题干" path="stem">
                      <NInput v-model:value="draftData.config.stem" :rows="3" placeholder="请输入题干" type="textarea"/>
                    </NFormItem>
                  </NForm>
                </n-card>
              </n-gi>
            </n-grid>
          </n-tab-pane>
          <n-tab-pane name="flag" tab="Flag管理">
            “威尔！着火了！快来帮忙！”我听到女朋友大喊。现在一个难题在我面前——是恢复一个重要的
            Amazon 服务，还是救公寓的火。<br><br>
            我的脑海中忽然出现了 Amazon
            著名的领导力准则”客户至上“，有很多的客户还依赖我们的服务，我不能让他们失望！所以着火也不管了，女朋友喊我也无所谓，我开始
            debug 这个线上问题。
          </n-tab-pane>
        </n-tabs>
      </div>
      <div v-else>
        未能加载草稿数据
      </div>
    </n-card>
  </div>
</template>

<style scoped>
/* 可以在这里添加样式 */
label {
  font-weight: bold;
}
</style>

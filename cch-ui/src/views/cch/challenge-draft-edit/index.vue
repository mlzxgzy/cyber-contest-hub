<script lang="ts" setup>
import {onMounted, ref, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {NButton, NCard, NEmpty, NFormItem, NInput, NInputNumber, NSelect, NSpace, NTag} from 'naive-ui';
import type {UploadFileInfo} from 'naive-ui';
import {fetchChallengeDraftByChallengeId, fetchGetChallengeById} from '@/service/api/cch/challenge';
import {
  fetchGetChallengeDraftById,
  fetchUpdateChallengeDraft
} from '@/service/api/cch/challenge-draft';
import {useTabStore} from '@/store/modules/tab';
import {useDict} from '@/hooks/business/dict';
import {useDownload} from '@/hooks/business/download';
import {useFormRules} from '@/hooks/common/form';
import FileUpload from '@/components/custom/file-upload.vue';
import {AcceptType} from '@/enum/business';
import {getRoutePath} from '@/router/elegant/transform';
import ChallengeDraftHistory from '@/views/cch/challenge-draft/modules/challenge-draft-history.vue';

defineOptions({
  name: 'ChallengeDraftEdit'
});

const route = useRoute();
const router = useRouter();
const {downloadChallengeFile} = useDownload();
const {removeActiveTab} = useTabStore();

const challengeId = ref<CommonType.IdType | null>(null);
const challengeData = ref<Api.Cch.Challenge>({} as Api.Cch.Challenge);
const draftId = ref<CommonType.IdType | null>(null);
const draftData = ref<Api.Cch.ChallengeDraft | null>(null);
const loading = ref(true);
const saving = ref(false);
const hasEdited = ref(false);
const dataInitialized = ref(false);
const challengeInitialized = ref(false);
const split = ref(0.8);

const {createRequiredRule} = useFormRules();

const {options: cchQuestionCategroyOptions} = useDict('cch_question_categroy');
const {options: cchQuestionDifficultyOptions} = useDict('cch_question_difficulty');

type challengeModel = Api.Cch.ChallengeOperateParams;
type challengeRuleKey = Extract<keyof challengeModel, 'id' | 'category' | 'name' | 'remark'>;
const challengeRules: Record<challengeRuleKey, App.Global.FormRule> = {
  id: createRequiredRule('主键不能为空'),
  category: createRequiredRule('题目类型不能为空'),
  name: createRequiredRule('题目名称不能为空'),
  remark: createRequiredRule('题目备注不能为空')
};
const draftRules: Record<string, App.Global.FormRule> = {};
const draftAttachmentRules: Record<string, App.Global.FormRule> = {};
const draftWriteupRules: Record<string, App.Global.FormRule> = {};

const draftAttachmentList = ref<UploadFileInfo[]>([]);
const draftWriteupList = ref<UploadFileInfo[]>([]);

// 历史组件引用
const historyRef = ref<InstanceType<typeof ChallengeDraftHistory> | null>(null);

function handleAttachmentUploadSuccess(data: Api.Cch.ChallengeFile) {
  if (!draftData.value) return;
  draftData.value.config.attachments ??= [];
  draftData.value.config.attachments = draftData.value.config.attachments.concat({
    fileId: data.id,
    fileName: data.originalName,
    fileUrl: data.url,
    remark: null
  });
}

function handleWriteupUploadSuccess(data: Api.Cch.ChallengeFile) {
  if (!draftData.value) return;
  draftData.value.config.writeups ??= [];
  draftData.value.config.writeups = draftData.value.config.writeups.concat({
    fileId: data.id,
    fileName: data.originalName,
    fileUrl: data.url,
    remark: null
  });
}

watch(
  draftData,
  () => {
    if (!dataInitialized.value) return;
    hasEdited.value = true;
  },
  {deep: true}
);

watch(
  challengeData,
  () => {
    if (!challengeInitialized.value) return;
    hasEdited.value = true;
  },
  {deep: true}
);

// 监听refresh参数变化，用于在tab已存在时刷新数据
watch(
  () => route.query.refresh,
  async (newVal, oldVal) => {
    // 只在refresh从非'true'变为'true'时触发，避免首次加载时重复刷新
    if (newVal === 'true' && oldVal !== 'true') {
      await handleRefresh();
    }
  }
);

function parseQueryId(raw: unknown) {
  if (Array.isArray(raw)) return raw[0] as CommonType.IdType;
  if (raw) return raw as CommonType.IdType;
  return null;
}

function applyDraftData(data: Api.Cch.ChallengeDraft) {
  dataInitialized.value = false;
  draftData.value = data;
  draftData.value.config.attachments ??= [];
  draftData.value.config.writeups ??= [];
  draftData.value.config.knowledge ??= [];
  draftData.value.config.flags ??= [];
  hasEdited.value = false;
  dataInitialized.value = true;
}

// Flag管理相关函数
function addFlag(type: 'static' | 'dynamic') {
  if (!draftData.value) return;
  draftData.value.config.flags ??= [];
  if (type === 'static') {
    draftData.value.config.flags.push({
      type: 'static',
      score: null,
      content: null,
      description: null,
      remark: null
    } as Api.Cch.ChallengeDraftConfigStaticFlag);
  } else {
    draftData.value.config.flags.push({
      type: 'dynamic',
      score: null,
      generatorConfig: null,
      description: null,
      remark: null
    } as Api.Cch.ChallengeDraftConfigDynamicFlag);
  }
}

function removeFlag(index: number) {
  if (!draftData.value || !draftData.value.config.flags) return;
  draftData.value.config.flags.splice(index, 1);
}

function getFlagTypeLabel(type: 'static' | 'dynamic') {
  return type === 'static' ? '静态' : '动态';
}

async function loadData(queryParams = route.query) {
  const currentChallengeId = parseQueryId(queryParams.challengeId);
  const currentDraftId = parseQueryId(queryParams.draftId);

  if (currentDraftId) {
    await loadDraftDataById(currentDraftId);
    if (draftData.value?.challengeId) {
      challengeId.value = draftData.value.challengeId;
      await loadChallengeData(draftData.value.challengeId);
    }
  } else if (currentChallengeId) {
    challengeId.value = currentChallengeId;
    await loadChallengeData(currentChallengeId);
    await loadDraftDataByChallengeId(currentChallengeId);
  } else {
    window.$message?.error('缺少必要的参数');
    router.back();
    return;
  }

  // 加载版本历史
  if (challengeId.value) {
    historyRef.value?.refresh();
  }

  loading.value = false;
}

// 处理refresh参数的函数
async function handleRefresh() {
  if (route.query.refresh === 'true') {
    loading.value = true;
    await loadData(route.query);
    // 移除refresh参数
    const query = {...route.query};
    delete query.refresh;
    router.replace({
      path: route.path,
      query
    });
  }
}

onMounted(async () => {
  // 如果存在refresh参数，直接处理刷新（内部会加载数据）
  // 否则正常加载数据
  if (route.query.refresh === 'true') {
    await handleRefresh();
  } else {
    await loadData();
  }
});

async function loadChallengeData(id: CommonType.IdType) {
  const {data, error} = await fetchGetChallengeById(id);
  if (error) {
    window.$message?.error(`获取题目数据失败: ${error}`);
    return;
  }
  challengeInitialized.value = false;
  challengeData.value = data;
  challengeInitialized.value = true;
  console.log('获取到的题目数据:', data);
}

async function loadDraftDataByChallengeId(id: CommonType.IdType) {
  const {data, error} = await fetchChallengeDraftByChallengeId(id);
  if (error) {
    window.$message?.error(`获取草稿数据失败: ${error}`);
    return;
  }
  applyDraftData(data);
  draftId.value = data.id;
  console.log('获取到的草稿数据:', data);
}

async function loadDraftDataById(id: CommonType.IdType) {
  const {data, error} = await fetchGetChallengeDraftById(id);
  if (error) {
    window.$message?.error(`获取草稿数据失败: ${error}`);
    return;
  }
  applyDraftData(data);
  draftId.value = data.id;
  console.log('获取到的草稿数据:', data);
}

async function saveDraft() {
  if (!draftData.value) return;

  saving.value = true;
  try {
    const {data, error} = await fetchUpdateChallengeDraft({
      id: draftData.value.id,
      challengeId: draftData.value.challengeId,
      challengeName: challengeData.value.name,
      // 由后端负责同步更新 Challenge.remark；这里单独传递，避免前端调用额外接口
      challengeRemark: challengeData.value.remark,
      challengeCategory: challengeData.value.category,
      challengeDescription: draftData.value.challengeDescription,
      config: draftData.value.config
    });

    if (error) {
      window.$message?.error(`保存失败: ${error}`);
      return;
    }

    if (data) {
      applyDraftData(data);
      draftId.value = data.id;
      challengeId.value = data.challengeId;
      router.replace({
        path: route.path,
        query: {
          ...route.query,
          challengeId: data.challengeId,
          draftId: data.id
        }
      });

      // 重新加载版本历史
      await historyRef.value?.refresh();
    }

    window.$message?.success('保存成功');
  } catch (err) {
    window.$message?.error(`保存异常: ${err}`);
  } finally {
    saving.value = false;
  }
}

function goBack() {
  removeActiveTab();
  router.push(getRoutePath('cch_challenge'));
}

function downloadFile(fileId: CommonType.IdType) {
  downloadChallengeFile(fileId);
}
</script>

<template>
  <NSplit v-model:size="split" class="p-20px" direction="horizontal" pane1-class="pr-10px" pane2-class="pl-10px">
    <template #1>
      <NCard title="题目草稿编辑">
        <template #header-extra>
          <NSpace>
            <NButton :loading="saving" :disabled="!hasEdited || saving" type="primary" @click="saveDraft">保存</NButton>
            <NButton @click="goBack">返回</NButton>
          </NSpace>
        </template>
        <NSkeleton v-if="loading" text :repeat="6"/>
        <div v-else-if="draftData" class="scrollbar">
          <NTabs animated default-value="info" type="segment">
            <NTabPane name="info" tab="题目信息">
              <NGrid cols="1 600:2 1200:3" x-gap="12" y-gap="12">
                <NGi>
                  <NCard :segmented="{ content: true }" title="基本信息">
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
                  </NCard>
                </NGi>
                <NGi>
                  <NCard :segmented="{ content: true }" title="题目信息">
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
                        <NInput
                          v-model:value="draftData.config.stem"
                          :rows="3"
                          placeholder="请输入题干"
                          type="textarea"
                        />
                      </NFormItem>
                      <NFormItem label="知识点" path="knowledge">
                        <NSelect v-model:value="draftData.config.knowledge" filterable multiple tag/>
                      </NFormItem>
                    </NForm>
                  </NCard>
                </NGi>
                <NGi>
                  <NCard :segmented="{ content: true }" title="附件管理">
                    <NForm ref="draftAttachmentFormRef" :model="draftData.config" :rules="draftAttachmentRules">
                      <NFormItem>
                        <FileUpload
                          v-model:file-list="draftAttachmentList"
                          upload-type="file"
                          :show-file-list="false"
                          :accept="AcceptType.ChallengeAttachment"
                          :data="{ challengeId: challengeId }"
                          action="/cch/challengeFile/upload"
                          :on-success="handleAttachmentUploadSuccess"
                        />
                      </NFormItem>
                      <NFormItem label="已上传附件">
                        <NSpace vertical class="w-full">
                          <template v-if="draftData.config.attachments?.length">
                            <NCard v-for="x of draftData.config.attachments" :key="x.fileId" size="small">
                              <div class="flex items-center justify-between gap-12px">
                                <div class="flex-1">
                                  <div class="mb-8px flex items-center gap-8px">
                                    <NTag type="success" size="small">附件</NTag>
                                    <span class="font-600">{{ x.fileName }}</span>
                                  </div>
                                  <NInput v-model:value="x.remark" placeholder="填写备注（可选）" size="small"/>
                                </div>
                                <div class="flex items-center">
                                  <NButton text type="primary" @click="downloadFile(x.fileId)">查看/下载</NButton>
                                </div>
                              </div>
                            </NCard>
                          </template>
                          <NEmpty v-else description="暂无附件"/>
                        </NSpace>
                      </NFormItem>
                    </NForm>
                  </NCard>
                </NGi>
                <NGi>
                  <NCard :segmented="{ content: true }" title="Writeup管理">
                    <NForm ref="draftWriteupFormRef" :model="draftData.config" :rules="draftWriteupRules">
                      <NFormItem>
                        <FileUpload
                          v-model:file-list="draftWriteupList"
                          upload-type="file"
                          :show-file-list="false"
                          :accept="AcceptType.ChallengeWriteup"
                          :data="{ challengeId: challengeId }"
                          action="/cch/challengeFile/upload"
                          :on-success="handleWriteupUploadSuccess"
                        />
                      </NFormItem>
                      <NFormItem label="已上传 Writeup">
                        <NSpace vertical class="w-full">
                          <template v-if="draftData.config.writeups?.length">
                            <NCard v-for="x of draftData.config.writeups" :key="x.fileId" size="small">
                              <div class="flex items-center justify-between gap-12px">
                                <div class="flex-1">
                                  <div class="mb-8px flex items-center gap-8px">
                                    <NTag type="info" size="small">Writeup</NTag>
                                    <span class="font-600">{{ x.fileName }}</span>
                                  </div>
                                  <NInput v-model:value="x.remark" placeholder="填写备注（可选）" size="small"/>
                                </div>
                                <div class="flex items-center">
                                  <NButton text type="primary" @click="downloadFile(x.fileId)">查看/下载</NButton>
                                </div>
                              </div>
                            </NCard>
                          </template>
                          <NEmpty v-else description="暂无 Writeup"/>
                        </NSpace>
                      </NFormItem>
                    </NForm>
                  </NCard>
                </NGi>
              </NGrid>
            </NTabPane>
            <NTabPane name="flag" tab="Flag管理">
              <NCard :segmented="{ content: true }" title="Flag列表">
                <NSpace vertical class="w-full">
                  <NSpace>
                    <NButton type="primary" @click="addFlag('static')">添加静态Flag</NButton>
                    <NButton @click="addFlag('dynamic')">添加动态Flag</NButton>
                  </NSpace>
                  <template v-if="draftData.config.flags?.length">
                    <NCard v-for="(flag, index) of draftData.config.flags" :key="index" size="small">
                      <template #header>
                        <div class="flex items-center justify-between">
                          <div class="flex items-center gap-8px">
                            <NTag :type="flag.type === 'static' ? 'success' : 'warning'" size="small">
                              {{ getFlagTypeLabel(flag.type) }}
                            </NTag>
                            <span>Flag {{ index + 1 }}</span>
                          </div>
                          <NButton text type="error" size="small" @click="removeFlag(index)">删除</NButton>
                        </div>
                      </template>
                      <NSpace vertical class="w-full">
                        <NGrid cols="1 800:2" x-gap="12" y-gap="12">
                          <NGi>
                            <NFormItem label="Flag类型">
                              <NSelect
                                :value="flag.type"
                                :options="[
                                  { label: '静态', value: 'static' },
                                  { label: '动态', value: 'dynamic' }
                                ]"
                                disabled
                              />
                            </NFormItem>
                          </NGi>
                          <NGi>
                            <NFormItem label="分值（推荐）">
                              <NInputNumber
                                v-model:value="flag.score"
                                :min="0"
                                :precision="0"
                                placeholder="请输入分值"
                                class="w-full"
                              />
                            </NFormItem>
                          </NGi>
                          <NGi v-if="flag.type === 'static'">
                            <NFormItem label="Flag内容">
                              <NInput
                                v-model:value="(flag as Api.Cch.ChallengeDraftConfigStaticFlag).content"
                                placeholder="请输入Flag内容"
                                type="textarea"
                                :rows="2"
                              />
                            </NFormItem>
                          </NGi>
                          <NGi v-if="flag.type === 'dynamic'">
                            <NFormItem label="生成规则配置">
                              <NInput
                                v-model:value="(flag as Api.Cch.ChallengeDraftConfigDynamicFlag).generatorConfig"
                                placeholder="动态Flag生成规则（待实现）"
                                type="textarea"
                                :rows="2"
                                disabled
                              />
                            </NFormItem>
                          </NGi>
                          <NGi>
                            <NFormItem label="Flag描述（给选手查看）">
                              <NInput
                                v-model:value="flag.description"
                                placeholder="请输入Flag描述，此内容会展示给选手"
                                type="textarea"
                                :rows="2"
                              />
                            </NFormItem>
                          </NGi>
                          <NGi>
                            <NFormItem label="Flag备注（仅后台可见）">
                              <NInput
                                v-model:value="flag.remark"
                                placeholder="请输入Flag备注，仅后台管理员可见"
                                type="textarea"
                                :rows="2"
                              />
                            </NFormItem>
                          </NGi>
                        </NGrid>
                      </NSpace>
                    </NCard>
                  </template>
                  <NEmpty v-else description="暂无Flag，点击上方按钮添加"/>
                </NSpace>
              </NCard>
            </NTabPane>
          </NTabs>
        </div>
        <div v-else>未能加载草稿数据</div>
      </NCard>
    </template>
    <template #2>
      <NCard>
        <NTabs type="line" placement="right">
          <NTabPane name="history" tab="修改历史">
            <ChallengeDraftHistory ref="historyRef" :challenge-id="challengeId" :current-draft-id="draftId"/>
          </NTabPane>
          <NTabPane name="oasis" tab="Oasis">Wonderwall</NTabPane>
        </NTabs>
      </NCard>
    </template>
  </NSplit>
</template>

<style scoped lang="scss">
/* 可以在这里添加样式 */
label {
  font-weight: bold;
}

.scrollbar {
  overflow-y: auto;
  padding-right: 15px;
  max-height: calc(100vh - 295px);
}
</style>

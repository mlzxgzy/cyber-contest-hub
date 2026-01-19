<script lang="ts" setup>
import {onMounted, ref, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {NButton, NCard, NEmpty, NInput, NSpace, NTag} from 'naive-ui';
import type {UploadFileInfo} from 'naive-ui';
import {fetchChallengeDraftByChallengeId, fetchGetChallengeById} from '@/service/api/cch/challenge';
import {
  fetchGetChallengeDraftById,
  fetchUpdateChallengeDraft
} from '@/service/api/cch/challenge-draft';
import {useDict} from "@/hooks/business/dict";
import {useDownload} from "@/hooks/business/download";
import {useFormRules} from "@/hooks/common/form";
import FileUpload from "@/components/custom/file-upload.vue";
import {AcceptType} from "@/enum/business";

defineOptions({
  name: 'ChallengeDraftEdit'
});

const route = useRoute();
const router = useRouter();
const {downloadChallengeFile} = useDownload();

const challengeId = ref<CommonType.IdType | null>(null);
const challengeData = ref<Api.Cch.Challenge>({} as Api.Cch.Challenge);
const draftId = ref<CommonType.IdType | null>(null);
const draftData = ref<Api.Cch.ChallengeDraft | null>(null);
const loading = ref(true);
const saving = ref(false);
const hasEdited = ref(false);
const dataInitialized = ref(false);
const split = ref(0.8);

const {createRequiredRule} = useFormRules();

const {options: cchQuestionCategroyOptions} = useDict('cch_question_categroy');
const {options: cchQuestionDifficultyOptions} = useDict('cch_question_difficulty');

type challengeModel = Api.Cch.ChallengeOperateParams;
type challengeRuleKey = Extract<keyof challengeModel,
  | 'id'
  | 'category'
  | 'name'
  | 'remark'>;
const challengeRules: Record<challengeRuleKey, App.Global.FormRule> = {
  id: createRequiredRule('主键不能为空'),
  category: createRequiredRule('题目类型不能为空'),
  name: createRequiredRule('题目名称不能为空'),
  remark: createRequiredRule('题目备注不能为空'),
};
const draftRules: Record<string, App.Global.FormRule> = {};
const draftAttachmentRules: Record<string, App.Global.FormRule> = {};
const draftWriteupRules: Record<string, App.Global.FormRule> = {};

const draftAttachmentList = ref<UploadFileInfo[]>([]);
const draftWriteupList = ref<UploadFileInfo[]>([]);

function mapToUploadList(items?: Api.Cch.ChallengeDraftConfigAttachment[]) {
  return (items ?? []).map(item => ({
    id: String(item.fileId),
    name: item.fileName,
    status: 'finished',
    url: item.fileUrl
  })) as UploadFileInfo[];
}

function syncConfigFromFileList(
  fileList: UploadFileInfo[],
  target: 'attachments' | 'writeups'
) {
  if (!draftData.value) return;
  const currentConfigList = draftData.value.config[target] ?? [];
  const remarkMap = new Map(
    currentConfigList.map(item => [String(item.fileId), item.remark ?? null])
  );
  draftData.value.config[target] = fileList
    .filter(file => file.status !== 'error')
    .map(file => ({
      fileId: file.id as CommonType.IdType,
      fileName: file.name || '',
      fileUrl: file.url || '',
      remark: remarkMap.get(String(file.id)) ?? null
    }));
}

function handleAttachmentUploadSuccess(data: Api.Cch.ChallengeFile) {
  if (!draftData.value) return;
  draftData.value.config.attachments ??= [];
  draftAttachmentList.value = mapToUploadList(draftData.value.config.attachments.concat({
    fileId: data.id,
    fileName: data.originalName,
    fileUrl: data.url,
    remark: null
  }));
}

function handleWriteupUploadSuccess(data: Api.Cch.ChallengeFile) {
  if (!draftData.value) return;
  draftData.value.config.writeups ??= [];
  draftWriteupList.value = mapToUploadList(draftData.value.config.writeups.concat({
    fileId: data.id,
    fileName: data.originalName,
    fileUrl: data.url,
    remark: null
  }));
}

watch(draftAttachmentList, list => syncConfigFromFileList(list, 'attachments'), {
  deep: true
});

watch(draftWriteupList, list => syncConfigFromFileList(list, 'writeups'), {
  deep: true
});

watch(draftData, () => {
  if (!dataInitialized.value) return;
  hasEdited.value = true;
}, {deep: true});

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
  draftAttachmentList.value = mapToUploadList(draftData.value.config.attachments);
  draftWriteupList.value = mapToUploadList(draftData.value.config.writeups);
  hasEdited.value = false;
  dataInitialized.value = true;
}

onMounted(async () => {
  challengeId.value = parseQueryId(route.query.challengeId);
  draftId.value = parseQueryId(route.query.draftId);

  if (draftId.value) {
    await loadDraftDataById(draftId.value);
    if (draftData.value?.challengeId) {
      challengeId.value = draftData.value.challengeId;
      await loadChallengeData(draftData.value.challengeId);
    }
  } else if (challengeId.value) {
    await loadChallengeData(challengeId.value);
    await loadDraftDataByChallengeId(challengeId.value);
  } else {
    window.$message?.error('缺少必要的参数');
    router.back();
  }
  loading.value = false;
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

async function loadDraftDataByChallengeId(id: CommonType.IdType) {
  const {data, error} = await fetchChallengeDraftByChallengeId(id);
  if (error) {
    window.$message?.error('获取草稿数据失败: ' + error);
    return;
  }
  applyDraftData(data);
  draftId.value = data.id;
  console.log('获取到的草稿数据:', data);
}

async function loadDraftDataById(id: CommonType.IdType) {
  const {data, error} = await fetchGetChallengeDraftById(id);
  if (error) {
    window.$message?.error('获取草稿数据失败: ' + error);
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
      challengeName: draftData.value.challengeName,
      challengeDescription: draftData.value.challengeDescription,
      config: draftData.value.config
    });

    if (error) {
      window.$message?.error('保存失败: ' + error);
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

function downloadFile(fileId: CommonType.IdType) {
  downloadChallengeFile(fileId);
}

</script>

<template>
  <n-split class="p-20px" v-model:size="split" direction="horizontal" pane1-class="pr-10px" pane2-class="pl-10px">
    <template #1>
      <n-card title="题目草稿编辑">
        <template #header-extra>
          <NSpace>
            <NButton :loading="saving" :disabled="!hasEdited || saving" type="primary" @click="saveDraft">保存</NButton>
            <NButton @click="goBack">返回</NButton>
          </NSpace>
        </template>
        <n-skeleton v-if="loading" text :repeat="6"/>
        <div v-else-if="draftData" class="scrollbar">
          <n-tabs animated default-value="info" type="segment">
            <n-tab-pane name="info" tab="题目信息">
              <n-grid cols="1 600:2 1200:3" x-gap="12" y-gap="12">
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
                        <NInput v-model:value="draftData.config.stem" :rows="3" placeholder="请输入题干"
                                type="textarea"/>
                      </NFormItem>
                      <NFormItem label="知识点" path="knowledge">
                        <n-select v-model:value="draftData.config.knowledge" filterable multiple tag/>
                      </NFormItem>
                    </NForm>
                  </n-card>
                </n-gi>
                <n-gi>
                  <n-card :segmented="{content: true}" title="附件管理">
                    <NForm ref="draftAttachmentFormRef" :model="draftData.config" :rules="draftAttachmentRules">
                      <NFormItem>
                        <FileUpload
                          v-model:file-list="draftAttachmentList"
                          upload-type="file"
                          :show-file-list="true"
                          :accept="AcceptType.ChallengeAttachment"
                          :data="{challengeId: challengeId}"
                          action="/cch/challengeFile/upload"
                          :on-success="handleAttachmentUploadSuccess"
                        />
                      </NFormItem>
                      <NFormItem label="已上传附件">
                        <NSpace vertical class="w-full">
                          <template v-if="draftData.config.attachments?.length">
                            <n-card v-for="x of draftData.config.attachments" :key="x.fileId" size="small">
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
                            </n-card>
                          </template>
                          <NEmpty v-else description="暂无附件"/>
                        </NSpace>
                      </NFormItem>
                    </NForm>
                  </n-card>
                </n-gi>
                <n-gi>
                  <n-card :segmented="{content: true}" title="Writeup管理">
                    <NForm ref="draftWriteupFormRef" :model="draftData.config" :rules="draftWriteupRules">
                      <NFormItem>
                        <FileUpload
                          v-model:file-list="draftWriteupList"
                          upload-type="file"
                          :show-file-list="true"
                          :accept="AcceptType.ChallengeWriteup"
                          :data="{challengeId: challengeId}"
                          action="/cch/challengeFile/upload"
                          :on-success="handleWriteupUploadSuccess"
                        />
                      </NFormItem>
                      <NFormItem label="已上传 Writeup">
                        <NSpace vertical class="w-full">
                          <template v-if="draftData.config.writeups?.length">
                            <n-card v-for="x of draftData.config.writeups" :key="x.fileId" size="small">
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
                            </n-card>
                          </template>
                          <NEmpty v-else description="暂无 Writeup"/>
                        </NSpace>
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
    </template>
    <template #2>
      <n-card>
        <n-tabs type="line" placement="right">
          <n-tab-pane name="oasis" tab="Oasis">
            Wonderwall
          </n-tab-pane>
        </n-tabs>
      </n-card>
    </template>
  </n-split>
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

<script lang="ts" setup>
import { computed, ref } from 'vue';
import {
  NForm,
  NSelect,
  NInput,
  NRadioButton,
  NRadioGroup,
  NEmpty,
  NButton,
  NModal,
  NSpin,
  useDialog
} from 'naive-ui';
import type { UploadFileInfo } from 'naive-ui';
import { useDict } from '@/hooks/business/dict';
import { useDownload } from '@/hooks/business/download';
import { useFormRules } from '@/hooks/common/form';
import FileUpload from '@/components/custom/file-upload.vue';
import { AcceptType } from '@/enum/business';
import { fetchBatchDeleteChallengeFile } from '@/service/api/cch/challenge-file';

const props = defineProps<{
  challengeData: Api.Cch.Challenge;
  draftData: Api.Cch.ChallengeDraft | null;
}>();

const { downloadChallengeFile, getChallengeFileBlob } = useDownload();
const dialog = useDialog();

const { createRequiredRule } = useFormRules();

type challengeModel = Api.Cch.ChallengeOperateParams;
type challengeRuleKey = Extract<keyof challengeModel, 'id' | 'category' | 'name'>;

const challengeRules: Record<challengeRuleKey, App.Global.FormRule> = {
  id: createRequiredRule('主键不能为空'),
  category: createRequiredRule('题目类型不能为空'),
  name: createRequiredRule('题目名称不能为空')
};

const draftRules: Record<string, App.Global.FormRule> = {};

const draftAttachmentList = ref<UploadFileInfo[]>([]);
const draftInternalAttachmentList = ref<UploadFileInfo[]>([]);
const draftWriteupList = ref<UploadFileInfo[]>([]);

// 上传时必须携带已创建的题目ID；创建模式下 challengeId 为空，不发送该字段（避免 "null" 字符串导致后端类型转换报错）
const uploadData = computed(() => {
  const cid = props.draftData?.challengeId;
  return cid ? { challengeId: cid } : undefined;
});

const { options: cchQuestionCategroyOptions } = useDict('cch_question_categroy');
const { options: cchQuestionDifficultyOptions } = useDict('cch_question_difficulty');
const { options: cchQuestionRunTypeOptions } = useDict('cch_question_run_type');

const challengeFormRef = ref<InstanceType<typeof NForm> | null>(null);
const draftFormRef = ref<InstanceType<typeof NForm> | null>(null);

function handleAttachmentUploadSuccess(data: Api.Cch.ChallengeFile) {
  if (!props.draftData) return;
  props.draftData.config.attachments ??= [];
  props.draftData.config.attachments = props.draftData.config.attachments.concat({
    fileId: data.id,
    fileName: data.originalName,
    fileUrl: data.url,
    remark: null
  });
  draftAttachmentList.value.length = 0;
}

function handleInternalAttachmentUploadSuccess(data: Api.Cch.ChallengeFile) {
  if (!props.draftData) return;
  props.draftData.config.internalAttachments ??= [];
  props.draftData.config.internalAttachments = props.draftData.config.internalAttachments.concat({
    fileId: data.id,
    fileName: data.originalName,
    fileUrl: data.url,
    remark: null
  });
  draftInternalAttachmentList.value.length = 0;
}

function handleWriteupUploadSuccess(data: Api.Cch.ChallengeFile) {
  if (!props.draftData) return;
  props.draftData.config.writeups ??= [];
  props.draftData.config.writeups = props.draftData.config.writeups.concat({
    fileId: data.id,
    fileName: data.originalName,
    fileUrl: data.url,
    remark: null
  });
  draftWriteupList.value.length = 0;
}

function downloadFile(fileId: CommonType.IdType) {
  downloadChallengeFile(fileId);
}

// ========== PDF 在线预览 ==========
const pdfPreviewVisible = ref(false);
const pdfPreviewLoading = ref(false);
const pdfPreviewUrl = ref('');
const pdfPreviewTitle = ref('');

function isPdf(fileName: string) {
  return !!fileName && fileName.toLowerCase().endsWith('.pdf');
}

async function previewPdf(fileId: CommonType.IdType, fileName: string) {
  pdfPreviewTitle.value = fileName;
  pdfPreviewVisible.value = true;
  pdfPreviewLoading.value = true;
  try {
    const blob = await getChallengeFileBlob(fileId);
    // 显式指定 PDF 类型，确保浏览器内置渲染器正确识别
    pdfPreviewUrl.value = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
  } catch (err) {
    pdfPreviewVisible.value = false;
    window.$message?.error(`加载预览失败: ${err}`);
  } finally {
    pdfPreviewLoading.value = false;
  }
}

function handlePdfPreviewClose(show: boolean) {
  if (!show) {
    // 释放 ObjectURL，避免内存泄漏
    if (pdfPreviewUrl.value) {
      window.URL.revokeObjectURL(pdfPreviewUrl.value);
      pdfPreviewUrl.value = '';
    }
    pdfPreviewLoading.value = false;
  }
}

function deleteAttachment(fileId: CommonType.IdType, fileName: string) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除附件「${fileName}」吗？删除后将无法恢复。`,
    positiveText: '确认删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        // 调用后端API删除文件
        const { error } = await fetchBatchDeleteChallengeFile([fileId]);
        if (error) {
          window.$message?.error(`删除失败: ${error}`);
          return;
        }

        // 从前端数据中移除
        if (props.draftData?.config.attachments) {
          const index = props.draftData.config.attachments.findIndex(item => item.fileId === fileId);
          if (index !== -1) {
            props.draftData.config.attachments.splice(index, 1);
          }
        }

        window.$message?.success('删除成功');
      } catch (err) {
        window.$message?.error(`删除异常: ${err}`);
      }
    }
  });
}

function deleteInternalAttachment(fileId: CommonType.IdType, fileName: string) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除内部附件「${fileName}」吗？删除后将无法恢复。`,
    positiveText: '确认删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        // 调用后端API删除文件
        const { error } = await fetchBatchDeleteChallengeFile([fileId]);
        if (error) {
          window.$message?.error(`删除失败: ${error}`);
          return;
        }

        // 从前端数据中移除
        if (props.draftData?.config.internalAttachments) {
          const index = props.draftData.config.internalAttachments.findIndex(item => item.fileId === fileId);
          if (index !== -1) {
            props.draftData.config.internalAttachments.splice(index, 1);
          }
        }

        window.$message?.success('删除成功');
      } catch (err) {
        window.$message?.error(`删除异常: ${err}`);
      }
    }
  });
}

function deleteWriteup(fileId: CommonType.IdType, fileName: string) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除 Writeup「${fileName}」吗？删除后将无法恢复。`,
    positiveText: '确认删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        // 调用后端API删除文件
        const { error } = await fetchBatchDeleteChallengeFile([fileId]);
        if (error) {
          window.$message?.error(`删除失败: ${error}`);
          return;
        }

        // 从前端数据中移除
        if (props.draftData?.config.writeups) {
          const index = props.draftData.config.writeups.findIndex(item => item.fileId === fileId);
          if (index !== -1) {
            props.draftData.config.writeups.splice(index, 1);
          }
        }

        window.$message?.success('删除成功');
      } catch (err) {
        window.$message?.error(`删除异常: ${err}`);
      }
    }
  });
}
</script>

<template>
  <div class="info-masonry">
    <div class="grid2">
      <!-- 基本信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <span class="card-dot basic"></span>
          <h3 class="card-title">基本信息</h3>
        </div>
        <div class="card-body">
          <NForm ref="challengeFormRef" :model="challengeData" :rules="challengeRules">
            <div class="frow">
              <div class="form-group">
                <label class="form-label">题目类型<span class="req">*</span></label>
                <NSelect
                  v-model:value="challengeData.category"
                  :options="cchQuestionCategroyOptions"
                  clearable
                  placeholder="请选择题目类型"
                  size="small"
                />
              </div>
              <div class="form-group">
                <label class="form-label">题目名称<span class="req">*</span></label>
                <NInput
                  v-model:value="challengeData.name"
                  placeholder="请输入题目名称"
                  size="small"
                />
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">题目备注</label>
              <NInput
                v-model:value="challengeData.remark"
                :rows="2"
                placeholder="请输入题目备注"
                type="textarea"
                size="small"
              />
            </div>
          </NForm>
        </div>
      </div>

      <!-- 题目配置卡片 -->
      <div class="info-card" v-if="draftData">
        <div class="card-header">
          <span class="card-dot config"></span>
          <h3 class="card-title">题目配置</h3>
        </div>
        <div class="card-body">
          <NForm ref="draftFormRef" :model="draftData.config" :rules="draftRules">
            <div class="form-group">
              <label class="form-label">运行类型</label>
              <NRadioGroup v-model:value="draftData.config.runType" size="small">
                <NRadioButton
                  v-for="option in cchQuestionRunTypeOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </NRadioGroup>
            </div>
            <div class="form-group">
              <label class="form-label">难度</label>
              <NRadioGroup v-model:value="draftData.config.difficulty" size="small">
                <NRadioButton
                  v-for="option in cchQuestionDifficultyOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </NRadioGroup>
            </div>
            <div class="form-group">
              <label class="form-label">知识点</label>
              <NSelect
                v-model:value="draftData.config.knowledge"
                filterable
                multiple
                tag
                size="small"
              />
            </div>
            <div class="form-group">
              <label class="form-label">题干描述</label>
              <NInput
                v-model:value="draftData.config.stem"
                :rows="4"
                placeholder="请输入题干内容"
                type="textarea"
                size="small"
              />
            </div>
          </NForm>
        </div>
      </div>
    </div>

    <div class="grid2">
      <!-- 选手附件卡片（对选手可见） -->
      <div class="info-card" v-if="draftData">
        <div class="card-header">
          <span class="card-dot attachment"></span>
          <h3 class="card-title">选手附件</h3>
          <span class="card-hint">{{ draftData.config.attachments?.length || 0 }} 个文件 · 选手可见</span>
          <FileUpload
            v-if="draftData.challengeId"
            v-model:file-list="draftAttachmentList"
            upload-type="file"
            trigger="button"
            :show-file-list="false"
            :show-tip="false"
            :accept="AcceptType.ChallengeAttachment"
            :data="uploadData"
            action="/cch/challengeFile/upload"
            :on-success="handleAttachmentUploadSuccess"
            class="card-upload"
          />
        </div>
        <div class="card-body">
          <div v-if="!draftData.challengeId" class="upload-disabled-tip">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
            请先点击「创建并保存草稿」创建题目后，再上传选手附件
          </div>
          <div v-if="draftData.config.attachments?.length" class="file-list">
            <div
              v-for="x of draftData.config.attachments"
              :key="x.fileId"
              class="file-item"
            >
              <div class="file-icon attachment">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                </svg>
              </div>
              <span class="file-name">{{ x.fileName }}</span>
              <NInput
                v-model:value="x.remark"
                placeholder="填写备注（可选）"
                size="tiny"
                class="file-remark"
              />
              <div class="file-acts">
                <NButton
                  v-if="isPdf(x.fileName)"
                  text
                  type="info"
                  size="tiny"
                  title="预览"
                  @click="previewPdf(x.fileId, x.fileName)"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                </NButton>
                <NButton
                  text
                  type="primary"
                  size="tiny"
                  title="下载"
                  @click="downloadFile(x.fileId)"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                    <polyline points="7 10 12 15 17 10" />
                    <line x1="12" y1="15" x2="12" y2="3" />
                  </svg>
                </NButton>
                <NButton
                  text
                  type="error"
                  size="tiny"
                  title="删除"
                  @click="deleteAttachment(x.fileId, x.fileName)"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6" />
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                  </svg>
                </NButton>
              </div>
            </div>
          </div>
          <NEmpty v-else-if="draftData.challengeId" description="暂无选手附件" size="small" class="empty-small" />
        </div>
      </div>

      <!-- 内部附件卡片（仅出题/管理端可见） -->
      <div class="info-card" v-if="draftData">
        <div class="card-header">
          <span class="card-dot internal"></span>
          <h3 class="card-title">内部附件</h3>
          <span class="card-hint">{{ draftData.config.internalAttachments?.length || 0 }} 个文件 · 仅管理端可见</span>
          <FileUpload
            v-if="draftData.challengeId"
            v-model:file-list="draftInternalAttachmentList"
            upload-type="file"
            trigger="button"
            :show-file-list="false"
            :show-tip="false"
            :accept="AcceptType.ChallengeAttachment"
            :data="uploadData"
            action="/cch/challengeFile/upload"
            :on-success="handleInternalAttachmentUploadSuccess"
            class="card-upload"
          />
        </div>
        <div class="card-body">
          <div v-if="!draftData.challengeId" class="upload-disabled-tip">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
            请先点击「创建并保存草稿」创建题目后，再上传内部附件
          </div>
          <div v-if="draftData.config.internalAttachments?.length" class="file-list">
            <div
              v-for="x of draftData.config.internalAttachments"
              :key="x.fileId"
              class="file-item"
            >
              <div class="file-icon internal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </div>
              <span class="file-name">{{ x.fileName }}</span>
              <NInput
                v-model:value="x.remark"
                placeholder="填写备注（可选）"
                size="tiny"
                class="file-remark"
              />
              <div class="file-acts">
                <NButton
                  v-if="isPdf(x.fileName)"
                  text
                  type="info"
                  size="tiny"
                  title="预览"
                  @click="previewPdf(x.fileId, x.fileName)"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                </NButton>
                <NButton
                  text
                  type="primary"
                  size="tiny"
                  title="下载"
                  @click="downloadFile(x.fileId)"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                    <polyline points="7 10 12 15 17 10" />
                    <line x1="12" y1="15" x2="12" y2="3" />
                  </svg>
                </NButton>
                <NButton
                  text
                  type="error"
                  size="tiny"
                  title="删除"
                  @click="deleteInternalAttachment(x.fileId, x.fileName)"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6" />
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                  </svg>
                </NButton>
              </div>
            </div>
          </div>
          <NEmpty v-else-if="draftData.challengeId" description="暂无内部附件" size="small" class="empty-small" />
        </div>
      </div>

      <!-- Writeup 管理卡片 -->
      <div class="info-card" v-if="draftData">
        <div class="card-header">
          <span class="card-dot writeup"></span>
          <h3 class="card-title">Writeup 管理</h3>
          <span class="card-hint">{{ draftData.config.writeups?.length || 0 }} 个文件</span>
          <FileUpload
            v-if="draftData.challengeId"
            v-model:file-list="draftWriteupList"
            upload-type="file"
            trigger="button"
            :show-file-list="false"
            :show-tip="false"
            :accept="AcceptType.ChallengeWriteup"
            :data="uploadData"
            action="/cch/challengeFile/upload"
            :on-success="handleWriteupUploadSuccess"
            class="card-upload"
          />
        </div>
        <div class="card-body">
          <div v-if="!draftData.challengeId" class="upload-disabled-tip">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
            请先点击「创建并保存草稿」创建题目后，再上传 Writeup
          </div>
          <div v-if="draftData.config.writeups?.length" class="file-list">
            <div
              v-for="x of draftData.config.writeups"
              :key="x.fileId"
              class="file-item"
            >
              <div class="file-icon writeup">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                  <line x1="16" y1="13" x2="8" y2="13" />
                  <line x1="16" y1="17" x2="8" y2="17" />
                </svg>
              </div>
              <span class="file-name">{{ x.fileName }}</span>
              <NInput
                v-model:value="x.remark"
                placeholder="填写备注（可选）"
                size="tiny"
                class="file-remark"
              />
              <div class="file-acts">
                <NButton
                  v-if="isPdf(x.fileName)"
                  text
                  type="info"
                  size="tiny"
                  title="预览"
                  @click="previewPdf(x.fileId, x.fileName)"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                </NButton>
                <NButton
                  text
                  type="primary"
                  size="tiny"
                  title="下载"
                  @click="downloadFile(x.fileId)"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                    <polyline points="7 10 12 15 17 10" />
                    <line x1="12" y1="15" x2="12" y2="3" />
                  </svg>
                </NButton>
                <NButton
                  text
                  type="error"
                  size="tiny"
                  title="删除"
                  @click="deleteWriteup(x.fileId, x.fileName)"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6" />
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                  </svg>
                </NButton>
              </div>
            </div>
          </div>
          <NEmpty v-else-if="draftData.challengeId" description="暂无 Writeup" size="small" class="empty-small" />
        </div>
      </div>
    </div>

    <!-- PDF 在线预览对话框 -->
    <NModal
      v-model:show="pdfPreviewVisible"
      preset="card"
      :title="pdfPreviewTitle"
      :style="{ width: '80vw' }"
      :bordered="false"
      :segmented="{ content: true }"
      @update:show="handlePdfPreviewClose"
    >
      <NSpin :show="pdfPreviewLoading">
        <iframe
          v-if="pdfPreviewUrl"
          :src="pdfPreviewUrl"
          class="pdf-preview-frame"
          frameborder="0"
          allowfullscreen
        />
        <div v-else class="pdf-preview-placeholder" />
      </NSpin>
    </NModal>
  </div>
</template>

<style scoped lang="scss">
// 紧凑扁平设计变量
$border-radius-sm: 2px;
$border-radius: 4px;
$border-color: #e5e7eb;
$bg-primary: #ffffff;
$bg-secondary: #f9fafb;
$bg-hover: #f3f4f6;
$shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
$shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);

// 双卡片并排（窄屏单列）
.info-masonry {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.grid2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  align-items: start;

  @media (max-width: 1100px) {
    grid-template-columns: 1fr;
  }
}

.info-card {
  background: $bg-primary;
  border-radius: $border-radius;
  border: 1px solid $border-color;
  box-shadow: $shadow-sm;
  overflow: hidden;
  transition: all 0.2s ease;

  &:hover {
    border-color: #d1d5db;
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 10px;
    background: $bg-secondary;
    border-bottom: 1px solid #eef0f2;
    min-height: 30px;
  }

  .card-dot {
    width: 8px;
    height: 8px;
    border-radius: 2px;
    flex-shrink: 0;

    &.basic {
      background: #3b82f6;
    }

    &.config {
      background: #06b6d4;
    }

    &.attachment {
      background: #10b981;
    }

    &.internal {
      background: #8b5cf6;
    }

    &.writeup {
      background: #f59e0b;
    }
  }

  .card-title {
    margin: 0;
    font-size: 12.5px;
    font-weight: 600;
    color: #1f2937;
    flex: 1;
    min-width: 0;
  }

  .card-hint {
    font-size: 11px;
    color: #9ca3af;
    font-weight: 400;
    flex-shrink: 0;
  }

  .card-upload {
    flex-shrink: 0;
    display: inline-block;
  }

  .card-body {
    padding: 10px 12px;
  }
}

.form-group {
  margin-bottom: 10px;

  &:last-child {
    margin-bottom: 0;
  }

  .form-label {
    display: block;
    font-size: 12px;
    font-weight: 500;
    color: #4b5563;
    margin-bottom: 4px;

    .req {
      color: #ef4444;
      margin-left: 2px;
    }
  }

  // 单选按钮组内边距收紧，避免窄卡片下溢出
  :deep(.n-radio-button) {
    padding: 0 10px;
  }
}

// 两个窄字段并排
.frow {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 12px;

  .form-group {
    min-width: 0;
  }
}

.file-list {
  display: flex;
  flex-direction: column;
}

.upload-disabled-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: $bg-secondary;
  border: 1px dashed #d1d5db;
  border-radius: 3px;
  color: #6b7280;
  font-size: 12px;

  svg {
    width: 14px;
    height: 14px;
    color: #3b82f6;
    flex-shrink: 0;
  }
}

// 紧凑文件行
.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border: 1px solid #eef0f2;
  border-radius: 3px;
  margin-bottom: 6px;
  background: #fafbfc;
  transition: all 0.15s ease;

  &:last-child {
    margin-bottom: 0;
  }

  &:hover {
    background: $bg-hover;

    .file-acts {
      opacity: 1;
    }
  }
}

.file-icon {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 3px;
  flex-shrink: 0;

  svg {
    width: 12px;
    height: 12px;
  }

  &.attachment {
    background: rgba(16, 185, 129, 0.1);
    color: #10b981;
  }

  &.internal {
    background: rgba(139, 92, 246, 0.1);
    color: #8b5cf6;
  }

  &.writeup {
    background: rgba(245, 158, 11, 0.1);
    color: #f59e0b;
  }
}

.file-name {
  flex-shrink: 0;
  max-width: 42%;
  font-size: 12px;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-remark {
  flex: 1;
  min-width: 0;

  :deep(.n-input) {
    background: transparent;

    .n-input__input-el {
      font-size: 11.5px;
    }

    // 默认隐藏边框保持文件行轻量，悬停/聚焦时显现
    .n-input__border,
    .n-input__state-border {
      border-color: transparent;
    }

    &:hover .n-input__state-border {
      border-color: #dcdfe4;
    }

    &.n-input--focus .n-input__state-border {
      border-color: #3b82f6;
    }
  }
}

.file-acts {
  display: flex;
  gap: 2px;
  opacity: 0.45;
  transition: opacity 0.15s ease;
  flex-shrink: 0;

  svg {
    width: 13px;
    height: 13px;
  }
}

.empty-small {
  padding: 12px 0;
}

// PDF 在线预览
.pdf-preview-frame {
  display: block;
  width: 100%;
  height: 75vh;
  border: none;
  background: #525659;
}

.pdf-preview-placeholder {
  height: 75vh;
  background: #525659;
}
</style>

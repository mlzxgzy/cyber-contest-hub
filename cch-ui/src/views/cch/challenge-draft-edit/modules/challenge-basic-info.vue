<script lang="ts" setup>
import { computed, ref } from 'vue';
import {
  NForm,
  NSelect,
  NInput,
  NRadioButton,
  NRadioGroup,
  NTag,
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
    <!-- 基本信息卡片 -->
    <div class="masonry-item">
      <div class="info-card">
        <div class="card-header">
          <div class="card-icon basic">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
              <line x1="3" y1="9" x2="21" y2="9" />
              <line x1="9" y1="21" x2="9" y2="9" />
            </svg>
          </div>
          <h3 class="card-title">基本信息</h3>
        </div>
        <div class="card-body">
          <NForm ref="challengeFormRef" :model="challengeData" :rules="challengeRules">
            <div class="form-group">
              <label class="form-label">题目类型</label>
              <NSelect
                v-model:value="challengeData.category"
                :options="cchQuestionCategroyOptions"
                clearable
                placeholder="请选择题目类型"
                class="cyber-input"
              />
            </div>
            <div class="form-group">
              <label class="form-label">题目名称</label>
              <NInput
                v-model:value="challengeData.name"
                placeholder="请输入题目名称"
                class="cyber-input"
              />
            </div>
            <div class="form-group">
              <label class="form-label">题目备注</label>
              <NInput
                v-model:value="challengeData.remark"
                :rows="3"
                placeholder="请输入题目备注"
                type="textarea"
                class="cyber-input"
              />
            </div>
          </NForm>
        </div>
      </div>
    </div>

    <!-- 题目配置卡片 -->
    <div class="masonry-item" v-if="draftData">
      <div class="info-card">
        <div class="card-header">
          <div class="card-icon config">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3" />
              <path
                d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"
              />
            </svg>
          </div>
          <h3 class="card-title">题目配置</h3>
        </div>
        <div class="card-body">
          <NForm ref="draftFormRef" :model="draftData.config" :rules="draftRules">
            <div class="form-group">
              <label class="form-label">运行类型</label><br>
              <NRadioGroup v-model:value="draftData.config.runType">
                <NRadioButton
                  v-for="option in cchQuestionRunTypeOptions"
                  :key="option.value"
                  :value="option.value"
                  :label="option.label"
                />
              </NRadioGroup>
            </div>
            <div class="form-group">
              <label class="form-label">难度</label><br>
              <NRadioGroup v-model:value="draftData.config.difficulty">
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
                class="cyber-select"
              />
            </div>
            <div class="form-group full-height">
              <label class="form-label">题干描述</label>
              <NInput
                v-model:value="draftData.config.stem"
                :rows="6"
                placeholder="请输入题干内容"
                type="textarea"
                class="cyber-input"
              />
            </div>
          </NForm>
        </div>
      </div>
    </div>

    <!-- 附件管理卡片 -->
    <div class="masonry-item" v-if="draftData">
      <div class="info-card">
        <div class="card-header">
          <div class="card-icon attachment">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path
                d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"
              />
            </svg>
          </div>
          <h3 class="card-title">附件管理</h3>
        </div>
        <div class="card-body">
          <FileUpload
            v-if="draftData.challengeId"
            v-model:file-list="draftAttachmentList"
            upload-type="file"
            :show-file-list="true"
            :accept="AcceptType.ChallengeAttachment"
            :data="uploadData"
            action="/cch/challengeFile/upload"
            :on-success="handleAttachmentUploadSuccess"
          />
          <div v-else class="upload-disabled-tip">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
            请先点击「创建并保存草稿」创建题目后，再上传附件
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
              <div class="file-info">
                <div class="file-name">
                  <NTag type="success" size="small">
                    附件
                  </NTag>
                  <span>{{ x.fileName }}</span>
                </div>
                <NInput
                  v-model:value="x.remark"
                  placeholder="填写备注（可选）"
                  size="small"
                  class="file-remark"
                />
              </div>
              <NButton
                v-if="isPdf(x.fileName)"
                text
                type="info"
                @click="previewPdf(x.fileId, x.fileName)"
                class="file-action"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
              </NButton>
              <NButton
                text
                type="primary"
                @click="downloadFile(x.fileId)"
                class="file-action"
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
                @click="deleteAttachment(x.fileId, x.fileName)"
                class="file-action"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                </svg>
              </NButton>
            </div>
          </div>
          <NEmpty v-else description="暂无附件" size="small" />
        </div>
      </div>
    </div>

    <!-- Writeup 管理卡片 -->
    <div class="masonry-item" v-if="draftData">
      <div class="info-card">
        <div class="card-header">
          <div class="card-icon writeup">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="16" y1="13" x2="8" y2="13" />
              <line x1="16" y1="17" x2="8" y2="17" />
              <polyline points="10 9 9 9 8 9" />
            </svg>
          </div>
          <h3 class="card-title">Writeup 管理</h3>
        </div>
        <div class="card-body">
          <FileUpload
            v-if="draftData.challengeId"
            v-model:file-list="draftWriteupList"
            upload-type="file"
            :show-file-list="true"
            :accept="AcceptType.ChallengeWriteup"
            :data="uploadData"
            action="/cch/challengeFile/upload"
            :on-success="handleWriteupUploadSuccess"
          />
          <div v-else class="upload-disabled-tip">
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
              <div class="file-info">
                <div class="file-name">
                  <NTag type="info" size="small">
                    Writeup
                  </NTag>
                  <span>{{ x.fileName }}</span>
                </div>
                <NInput
                  v-model:value="x.remark"
                  placeholder="填写备注（可选）"
                  size="small"
                  class="file-remark"
                />
              </div>
              <NButton
                v-if="isPdf(x.fileName)"
                text
                type="info"
                @click="previewPdf(x.fileId, x.fileName)"
                class="file-action"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
              </NButton>
              <NButton
                text
                type="primary"
                @click="downloadFile(x.fileId)"
                class="file-action"
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
                @click="deleteWriteup(x.fileId, x.fileName)"
                class="file-action"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                </svg>
              </NButton>
            </div>
          </div>
          <NEmpty v-else description="暂无 Writeup" size="small" />
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
// 扁平化设计变量
$border-radius-sm: 2px;
$border-radius: 4px;
$border-color: #e5e7eb;
$bg-primary: #ffffff;
$bg-secondary: #f9fafb;
$bg-hover: #f3f4f6;
$shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
$shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);

.info-masonry {
  column-count: 1;
  column-gap: 16px;

  @media (min-width: 600px) {
    column-count: 2;
  }
}

.masonry-item {
  break-inside: avoid;
  margin-bottom: 16px;
}

.info-card {
  background: $bg-primary;
  border-radius: $border-radius;
  border: 1px solid $border-color;
  box-shadow: $shadow-sm;
  overflow: hidden;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: $shadow;
    border-color: #d1d5db;
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    background: $bg-secondary;
    border-bottom: 1px solid $border-color;
  }

  .card-icon {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $border-radius-sm;
    color: #ffffff;
    flex-shrink: 0;

    svg {
      width: 18px;
      height: 18px;
    }

    &.basic {
      background: #3b82f6;
    }

    &.config {
      background: #06b6d4;
    }

    &.attachment {
      background: #10b981;
    }

    &.writeup {
      background: #f59e0b;
    }
  }

  .card-title {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
    letter-spacing: 0.01em;
  }

  .card-body {
    padding: 16px;
  }
}

.form-group {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }

  .form-label {
    display: block;
    font-size: 13px;
    font-weight: 500;
    color: #374151;
    margin-bottom: 8px;
  }

  .cyber-input,
  .cyber-select {
    :deep(.n-input),
    :deep(.n-select) {
      border-radius: $border-radius-sm;
    }
  }
}

.file-list {
  margin-top: 16px;
}

.upload-disabled-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  background: #f9fafb;
  border: 1px dashed #d1d5db;
  border-radius: 4px;
  color: #6b7280;
  font-size: 13px;

  svg {
    width: 16px;
    height: 16px;
    color: #3b82f6;
    flex-shrink: 0;
  }
}

.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: $bg-secondary;
  border: 1px solid $border-color;
  border-radius: $border-radius-sm;
  margin-bottom: 8px;
  transition: all 0.2s ease;

  &:last-child {
    margin-bottom: 0;
  }

  &:hover {
    background: $bg-hover;
    border-color: #d1d5db;
  }
}

.file-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $border-radius-sm;
  flex-shrink: 0;

  svg {
    width: 16px;
    height: 16px;
  }

  &.attachment {
    background: rgba(16, 185, 129, 0.1);
    color: #10b981;
  }

  &.writeup {
    background: rgba(245, 158, 11, 0.1);
    color: #f59e0b;
  }
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-remark {
  :deep(.n-input) {
    background: transparent;
    border: none;
    font-size: 12px;
    border-radius: $border-radius-sm;

    .n-input__input-el {
      text-overflow: ellipsis;
    }
  }
}

.file-action {
  flex-shrink: 0;

  svg {
    width: 16px;
    height: 16px;
  }
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


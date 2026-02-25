<script lang="ts" setup>
import {computed, h, ref} from 'vue';
import type {DataTableColumns, UploadFileInfo} from 'naive-ui';
import {NButton, NSpace} from 'naive-ui';
import {fetchDownloadContestFile, fetchRemoveContestFile} from '@/service/api/cch/project';
import {AcceptType} from '@/enum/business';
import FileUpload from '@/components/custom/file-upload.vue';

defineOptions({
  name: 'ProjectFileManage'
});

interface Props {
  projectId: CommonType.IdType;
  projectType: 'normal' | 'contest';
  isProjectAdmin?: boolean;
  isSuperAdmin?: boolean;
  files?: Api.Cch.ContestFile[];
}

const props = withDefaults(defineProps<Props>(), {
  files: () => []
});

interface Emits {
  (e: 'refresh'): void;
}

const emit = defineEmits<Emits>();

const loading = ref(false);
const fileList = ref<UploadFileInfo[]>([]);
const fileTag = ref('');

const files = computed(() => props.files || []);

const canManage = computed(() => {
  return !!(props.isProjectAdmin || props.isSuperAdmin);
});

const isContestProject = computed(() => props.projectType === 'contest');

// Define columns for NDataTable
const columns = computed<DataTableColumns<Api.Cch.ContestFile>>(() => [
  {
    key: 'fileName',
    title: '文件名',
    align: 'center',
    minWidth: 200
  },
  {
    key: 'fileTag',
    title: '标签',
    align: 'center',
    minWidth: 120
  },
  {
    key: 'createTime',
    title: '上传时间',
    align: 'center',
    minWidth: 160
  },
  {
    key: 'operate',
    title: '操作',
    align: 'center',
    width: 200,
    render: row => {
      const buttons = [
        h(NButton, {
          text: true,
          type: 'primary',
          size: 'small',
          onClick: () => handleDownloadFile(row.id)
        }, { default: () => '下载' })
      ];
      
      if (canManage.value) {
        buttons.push(
          h(NButton, {
            text: true,
            type: 'error',
            size: 'small',
            onClick: () => handleDeleteFile(row.id)
          }, { default: () => '删除' })
        );
      }
      
      return h(NSpace, null, { default: () => buttons });
    }
  }
]);

// Files are loaded from parent component

function handleFileUploadSuccess(fileData: any) {
  // FileUpload component handles the upload, we just need to refresh
  // The response should be ContestFile from the project upload endpoint
  fileList.value = [];
  fileTag.value = '';
  emit('refresh');
}

async function handleDownloadFile(fileId: CommonType.IdType) {
  // Download contest file using the ContestFile id
  const {data, error} = await fetchDownloadContestFile(fileId);
  if (error || !data) return;
  
  // Handle blob download
  const file = files.value.find(f => f.id === fileId);
  const fileName = file?.fileName || `file_${fileId}`;
  const url = window.URL.createObjectURL(data);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

async function handleDeleteFile(fileId: CommonType.IdType) {
  const {error} = await fetchRemoveContestFile([fileId]);
  if (error) return;

  window.$message?.success('删除成功');
  emit('refresh');
}

// Files are passed as props from parent

</script>

<template>
  <div v-if="isContestProject" class="flex-col-stretch gap-16px">
    <NCard v-if="canManage" :bordered="false" size="small" title="上传文件">
      <NForm label-placement="left" :label-width="100">
        <NFormItem label="文件标签">
          <NInput
            v-model:value="fileTag"
            placeholder="请输入文件标签（可选）"
            clearable
          />
        </NFormItem>
        <NFormItem label="选择文件">
          <FileUpload
            v-model:file-list="fileList"
            upload-type="file"
            :accept="AcceptType.File"
            :file-size="10"
            :max="1"
            :data="{fileTag: fileTag || 'default'}"
            :action="`/cch/project/${projectId}/files/upload`"
            @on-success="handleFileUploadSuccess"
          />
        </NFormItem>
      </NForm>
    </NCard>

    <NCard :bordered="false" size="small" title="文件列表">
      <NSpin :show="loading">
        <NDataTable
          :columns="columns"
          :data="files"
          :row-key="row => row.id"
          size="small"
        />
      </NSpin>
    </NCard>
  </div>
  <NEmpty v-else description="文件管理仅适用于竞赛项目"/>
</template>

<style scoped></style>

<script setup lang="ts">
import {computed, ref, watch} from 'vue';
import type {UploadFileInfo} from 'naive-ui';
import FileUpload from '@/components/custom/file-upload.vue';
import {AcceptType} from '@/enum/business';

defineOptions({
  name: 'ChallengeFileUploadModal'
});

interface Props {
  challengeId: string,
  showChallengeId: false,
  uploadType: 'attachment' | 'writeup';
}

const props = defineProps<Props>();
const localChallengeId = ref<string | null>(props.challengeId);

interface Emits {
  (e: 'close'): void;
}

const emit = defineEmits<Emits>();

const visible = defineModel<boolean>('visible', {
  default: false
});

const accept = computed(() => (props.uploadType === 'attachment' ? AcceptType.ChallengeAttachment : AcceptType.ChallengeWriteup));

const fileList = ref<UploadFileInfo[]>([]);

function handleUpdateModelWhenUpload() {
  fileList.value = [];
}

function closeDrawer() {
  visible.value = false;
}

function handleClose() {
  closeDrawer();
  if (fileList.value?.length > 0) {
    emit('close');
  }
}

watch(visible, () => {
  if (visible.value) {
    handleUpdateModelWhenUpload();
  }
});
</script>

<template>
  <NModal
      v-model:show="visible"
      class="max-h-520px max-w-90% w-600px"
      preset="card"
      :title="`上传${uploadType === 'attachment' ? '附件' : 'WriteUp'}`"
      size="huge"
      :bordered="false"
      @after-leave="handleClose"
  >
    <n-form-item v-if="showChallengeId" label-placement="left" label="赛题ID">
      <n-input v-model:value="localChallengeId" type="text" placeholder="请输入赛题ID"/>
    </n-form-item>
    <FileUpload v-model:file-list="fileList" upload-type="file" :accept="accept"
                :data="{challengeId: localChallengeId}" action="/cch/challengeFile/upload"/>
  </NModal>
</template>

<style scoped></style>

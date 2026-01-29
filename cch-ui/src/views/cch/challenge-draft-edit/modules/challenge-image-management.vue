<script setup lang="ts">
import {onMounted, ref, watch} from 'vue';
import {
  NButton,
  NCard,
  NEmpty,
  NProgress,
  NTag,
  NText,
  NUpload,
  NUploadDragger,
  NP,
  NSpace
} from 'naive-ui';
import type {UploadFileInfo} from 'naive-ui';
import {
  fetchGetChallengeContainerImageByChallengeId,
  fetchUploadChallengeContainerImage,
  fetchManualLoadImage,
  fetchBatchDeleteChallengeContainerImage
} from '@/service/api/cch/challenge-container-image';

interface Props {
  challengeId: CommonType.IdType | null;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'update'): void;
}

const emit = defineEmits<Emits>();

const containerImages = ref<Api.Cch.ChallengeContainerImage[]>([]);
const isRefreshDisabled = ref(false); // 控制刷新按钮是否禁用

// 节流版本的加载列表函数
async function throttledLoadImageList() {
  if (isRefreshDisabled.value) return; // 如果正在禁用期，则直接返回

  isRefreshDisabled.value = true; // 立即禁用按钮

  try {
    await loadImageList();
  } finally {
    // 无论成功或失败，都在2秒后恢复按钮可用
    setTimeout(() => {
      isRefreshDisabled.value = false;
    }, 2000);
  }
}

// 加载镜像列表
async function loadImageList() {
  if (!props.challengeId) return;

  try {
    const {data, error} = await fetchGetChallengeContainerImageByChallengeId(props.challengeId);
    if (error) {
      window.$message?.error(`获取镜像列表失败: ${error}`);
      return;
    }
    containerImages.value = data;
  } catch (err) {
    window.$message?.error(`获取镜像列表异常: ${err}`);
  }
}

// 上传镜像
async function handleImageUpload(options: {
  file: UploadFileInfo;
  data?: Record<string, string>;
  headers?: Record<string, string>;
  withCredentials?: boolean;
  action?: string;
  onFinish: () => void;
  onError: () => void;
}) {
  const file = options.file;

  if (!props.challengeId) {
    window.$message?.error('缺少必要的题目ID信息');
    options.onError();
    return;
  }

  const formData = new FormData();
  formData.append('challengeId', props.challengeId.toString());
  formData.append('imageName', (file.name || '').split('.')[0] || 'default-image');

  // 正确添加文件对象
  if (file.file instanceof File) {
    formData.append('file', file.file);
  } else {
    window.$message?.error('文件对象格式不正确');
    options.onError();
    return;
  }

  try {
    const {data, error} = await fetchUploadChallengeContainerImage(formData);

    if (error) {
      window.$message?.error(`上传失败: ${error}`);
      options.onError();
      return;
    }

    // 触发更新事件
    emit('update');
    window.$message?.success('上传成功');
    // 重新加载列表
    await loadImageList();
    options.onFinish();
  } catch (err) {
    window.$message?.error(`上传异常: ${err}`);
    options.onError();
  }
}

// 获取状态类型
function getStatusType(status: string) {
  switch (status) {
    case 'uploading':
      return 'info';
    case 'uploaded':
    case 'available':
      return 'success';
    case 'validating':
      return 'warning';
    case 'error':
      return 'error';
    default:
      return 'default';
  }
}

// 获取状态文本
function getImageStatusText(status: string) {
  switch (status) {
    case 'uploading':
      return '上传中';
    case 'uploaded':
      return '已上传';
    case 'validating':
      return '验证中';
    case 'available':
      return '可用';
    case 'error':
      return '错误';
    default:
      return status;
  }
}

// 格式化文件大小
function formatFileSize(size?: number) {
  if (size === undefined || size === null) return '-';

  const units = ['B', 'KB', 'MB', 'GB'];
  let unitIndex = 0;
  let fileSize = size;

  while (fileSize >= 1024 && unitIndex < units.length - 1) {
    fileSize /= 1024;
    unitIndex++;
  }

  return `${fileSize.toFixed(2)} ${units[unitIndex]}`;
}

// 手动Load镜像到Docker
async function handleManualLoadImage(imageId: CommonType.IdType) {
  try {
    const {data, error} = await fetchManualLoadImage(imageId);

    if (error) {
      window.$message?.error(`Load镜像失败: ${error}`);
      return;
    }

    if (data) {
      window.$message?.success('Load镜像成功');
      // 触发更新事件
      emit('update');
      // Load操作完成后自动刷新镜像列表
      await loadImageList();
    } else {
      window.$message?.error('Load镜像失败');
    }
  } catch (err) {
    window.$message?.error(`Load镜像异常: ${err}`);
  }
}

// 删除镜像
async function handleDeleteImage(imageId: CommonType.IdType) {
  try {
    const {error} = await fetchBatchDeleteChallengeContainerImage([imageId]);

    if (error) {
      window.$message?.error(`删除镜像失败: ${error}`);
      return;
    }

    window.$message?.success('删除镜像成功');
    // 触发更新事件
    emit('update');
    // 重新加载列表
    await loadImageList();
  } catch (err) {
    window.$message?.error(`删除镜像异常: ${err}`);
  }
}


// 监听challengeId变化，重新加载数据
watch(() => props.challengeId, async (newId) => {
  if (newId) {
    await loadImageList();
  }
});

onMounted(async () => {
  if (props.challengeId) {
    await loadImageList();
  }
});
</script>

<template>
  <div class="p-4">
    <h3 class="mb-4">容器镜像管理</h3>

    <!-- 镜像上传区域 -->
    <NCard title="上传镜像" class="mb-4">
      <NUpload
        :max="1"
        accept=".tar,.tar.gz,.zip"
        :custom-request="handleImageUpload"
        :show-download-button="false"
      >
        <NUploadDragger>
          <div class="mb-3">
            <icon-mdi-cloud-upload class="text-48px text-gray-400"/>
          </div>
          <NText class="text-16px">点击或者拖动镜像到该区域</NText>
          <NP depth="3" class="mt-12px text-center">
            支持的格式: tar, tar.gz, zip
          </NP>
        </NUploadDragger>
      </NUpload>
    </NCard>

    <!-- 已上传镜像列表 -->
    <NCard title="已上传镜像">
      <template #header-extra>
        <NButton size="tiny" @click="throttledLoadImageList" :disabled="isRefreshDisabled" type="primary">
          {{ isRefreshDisabled ? '刷新中...' : '刷新列表' }}
        </NButton>
      </template>
      <NSpace vertical class="w-full">
        <template v-if="containerImages.length > 0">
          <NCard
            v-for="image of containerImages"
            :key="image.id"
            size="small"
            :class="image.status === 'error' ? 'bg-red-50' : ''"
          >
            <div class="flex items-center justify-between gap-12px">
              <div class="flex-1">
                <div class="mb-2 flex items-center gap-8px">
                  <NTag :type="getStatusType(image.status)" size="small">
                    {{ getImageStatusText(image.status) }}
                  </NTag>
                  <span class="font-600">{{ image.imageName.replace(/cch\/\d+\//, '') }}:{{ image.imageTag }}</span>
                  <span class="text-gray-500 text-sm">{{ formatFileSize(image.imageSize) }}</span>
                </div>

                <!-- 进度条 -->
                <div v-if="['uploading', 'validating'].includes(image.status)" class="mb-2">
                  <NProgress
                    type="line"
                    :percentage="image.progress || 0"
                    :status="image.status === 'error' ? 'error' : 'processing'"
                    indicator-text-color="#000"
                  />
                  <div class="text-xs text-gray-500 mt-1">
                    {{ image.status === 'uploading' ? '上传中...' : '验证中...' }}
                  </div>
                </div>

                <!-- 错误信息 -->
                <div v-if="image.status === 'error'" class="text-red-500 text-sm mb-2">
                  错误: {{ image.errorMessage }}
                </div>
              </div>
              <div class="flex flex-col items-end gap-2">
                <!-- Load按钮 - 仅在状态为uploaded时显示 -->
                <NButton
                  v-if="image.status === 'uploaded'"
                  type="primary"
                  size="small"
                  @click="handleManualLoadImage(image.id)"
                >
                  Load到Docker
                </NButton>
                <NButton
                  text
                  type="error"
                  @click="handleDeleteImage(image.id)"
                >
                  删除
                </NButton>
              </div>
            </div>
          </NCard>
        </template>
        <NEmpty v-else description="暂无镜像，请先上传"/>
      </NSpace>
    </NCard>
  </div>
</template>

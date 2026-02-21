<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue';
import {
  NButton,
  NCard,
  NDivider,
  NEmpty,
  NFormItem,
  NGrid,
  NGi,
  NInput,
  NInputNumber,
  NSelect,
  NSpace
} from 'naive-ui';
import {fetchGetChallengeContainerImageByChallengeId} from '@/service/api/cch/challenge-container-image';
import EnvMapEditor from '../components/EnvMapEditor.vue';
import PortsMapEditor from '../components/PortsMapEditor.vue';

interface Props {
  modelValue?: Api.Cch.ChallengeDraftContainerTarget[];
  challengeId: CommonType.IdType | null;
  challengeName?: string | null;
}

interface Emits {
  (e: 'update:modelValue', value: Api.Cch.ChallengeDraftContainerTarget[]): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const targets = computed<Api.Cch.ChallengeDraftContainerTarget[]>({
  get() {
    return props.modelValue ?? [];
  },
  set(v) {
    emit('update:modelValue', v);
  }
});

const containerImages = ref<Api.Cch.ChallengeContainerImage[]>([]);

const imageOptions = computed(() => {
  return (containerImages.value || []).map(img => {
    const label = `${img.imageName} (${img.status})`;
    return {label, value: img.id};
  });
});

async function loadImages() {
  if (!props.challengeId) return;
  const {data, error} = await fetchGetChallengeContainerImageByChallengeId(props.challengeId);
  if (error) {
    window.$message?.error(`获取镜像列表失败: ${error}`);
    return;
  }
  containerImages.value = data || [];
}

watch(
  () => props.challengeId,
  async () => {
    await loadImages();
  }
);

onMounted(async () => {
  await loadImages();
});

function ensureTargetDefaults(t: Api.Cch.ChallengeDraftContainerTarget) {
  t.env ??= {};
  t.ports ??= {};
  t.resources ??= {cpuLimit: null, memoryLimit: null};
}

watch(
  targets,
  v => {
    v.forEach(ensureTargetDefaults);
  },
  {immediate: true, deep: true}
);

/**
 * 生成随机8位数字字符
 */
function generateRandomDigits(length: number = 8): string {
  return Array.from({ length }, () => Math.floor(Math.random() * 10)).join('');
}

function addTarget() {
  const next = targets.value.slice();
  // 自动生成名称：本题名称-随机8位数字字符
  const challengeName = props.challengeName || '靶机';
  const randomDigits = generateRandomDigits(8);
  const autoName = `${challengeName}-${randomDigits}`;
  
  next.push({
    name: autoName,
    imageId: null,
    imageName: null,
    env: {},
    ports: {},
    resources: {cpuLimit: null, memoryLimit: null}
  });
  targets.value = next;
}

function removeTarget(idx: number) {
  const next = targets.value.slice();
  next.splice(idx, 1);
  targets.value = next;
}

function resolveImageName(imageId?: CommonType.IdType | null) {
  if (!imageId) return null;
  const found = containerImages.value.find(x => x.id === imageId);
  if (!found) return null;
  // 优先使用镜像拉取地址作为实际运行镜像名称；否则退回展示名称
  return found.pullAddress || found.imageName || null;
}
</script>

<template>
  <div class="container-target-config">
    <div class="config-header">
      <div class="config-title">容器靶机配置</div>
      <NSpace>
        <NButton type="primary" @click="addTarget">新增靶机</NButton>
        <NButton @click="loadImages">刷新镜像列表</NButton>
      </NSpace>
    </div>

    <NSpace vertical class="w-full">
      <template v-if="targets.length">
        <NCard v-for="(t, idx) in targets" :key="idx" size="small" class="target-card">
          <template #header>
            <div class="target-card-header">
              <div class="target-title">靶机 {{ idx + 1 }}</div>
              <NButton text type="error" size="small" @click="removeTarget(idx)">删除</NButton>
            </div>
          </template>

          <NGrid cols="1 900:2" x-gap="12" y-gap="12">
            <NGi>
              <NFormItem label="名称" required>
                <NInput v-model:value="t.name" placeholder="例如：web / pwn / db"/>
              </NFormItem>
            </NGi>
            <NGi>
              <NFormItem label="镜像">
                <NSelect
                  v-model:value="t.imageId"
                  :options="imageOptions"
                  clearable
                  placeholder="请选择本题已上传的镜像"
                  @update:value="() => (t.imageName = resolveImageName(t.imageId))"
                />
              </NFormItem>
            </NGi>
          </NGrid>

          <NDivider/>

          <NGrid cols="1 900:2" x-gap="12" y-gap="12">
            <NGi>
              <NFormItem label="资源限制 - CPU (millicores)">
                <NInputNumber
                  v-model:value="t.resources!.cpuLimit"
                  :min="0"
                  :precision="0"
                  placeholder="例如：500 表示 0.5 核"
                  class="w-full"
                />
              </NFormItem>
            </NGi>
            <NGi>
              <NFormItem label="资源限制 - 内存 (MB)">
                <NInputNumber
                  v-model:value="t.resources!.memoryLimit"
                  :min="0"
                  :precision="0"
                  placeholder="例如：256"
                  class="w-full"
                />
              </NFormItem>
            </NGi>
          </NGrid>

          <NDivider/>

          <NFormItem label="环境变量 (Map<string,string>)">
            <EnvMapEditor v-model="t.env"/>
          </NFormItem>

          <NDivider/>

          <NFormItem label="开放端口 (Map<name,entity>)">
            <PortsMapEditor v-model="t.ports"/>
          </NFormItem>
        </NCard>
      </template>
      <NEmpty v-else description="暂无靶机配置，点击'新增靶机'开始配置"/>
    </NSpace>
  </div>
</template>

<style scoped lang="scss">
// 扁平化设计变量
$border-radius-sm: 2px;
$border-radius: 4px;
$border-color: #e5e7eb;
$bg-primary: #ffffff;
$bg-secondary: #f9fafb;
$shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
$shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);

.container-target-config {
  padding: 16px;
}

.config-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.config-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  letter-spacing: 0.01em;
}

.target-card {
  border-radius: $border-radius;
  border: 1px solid $border-color;
  box-shadow: $shadow-sm;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: $shadow;
    border-color: #d1d5db;
  }

  :deep(.n-card__header) {
    padding: 14px 16px;
    border-bottom: 1px solid $border-color;
    background: $bg-secondary;
  }

  :deep(.n-card__content) {
    padding: 16px;
  }
}

.target-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.target-title {
  font-weight: 600;
  font-size: 14px;
  color: #1f2937;
}

:deep(.n-divider) {
  margin: 16px 0;
}

:deep(.n-form-item-label) {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

:deep(.n-input),
:deep(.n-select),
:deep(.n-input-number) {
  border-radius: $border-radius-sm;
}
</style>

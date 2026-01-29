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
    const label = `${img.imageName?.replace(/^cch\/\d+\//, '')}:${img.imageTag} (${img.status})`;
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

function addTarget() {
  const next = targets.value.slice();
  next.push({
    name: null,
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
  return `${found.imageName}:${found.imageTag}`;
}
</script>

<template>
  <div class="p-4">
    <div class="mb-12px flex items-center justify-between">
      <div class="text-16px font-600">容器靶机配置</div>
      <NSpace>
        <NButton secondary type="primary" @click="addTarget">新增靶机</NButton>
        <NButton secondary @click="loadImages">刷新镜像列表</NButton>
      </NSpace>
    </div>

    <NSpace vertical class="w-full">
      <template v-if="targets.length">
        <NCard v-for="(t, idx) in targets" :key="idx" size="small">
          <template #header>
            <div class="flex items-center justify-between">
              <div class="font-600">靶机 {{ idx + 1 }}</div>
              <NButton text type="error" @click="removeTarget(idx)">删除</NButton>
            </div>
          </template>

          <NGrid cols="1 900:2" x-gap="12" y-gap="12">
            <NGi>
              <NFormItem label="名称">
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

          <NFormItem label="环境变量 (Map&lt;string,string&gt;)">
            <EnvMapEditor v-model="t.env"/>
          </NFormItem>

          <NDivider/>

          <NFormItem label="开放端口 (Map&lt;name,entity&gt;)">
            <PortsMapEditor v-model="t.ports"/>
          </NFormItem>
        </NCard>
      </template>
      <NEmpty v-else description="暂无靶机配置，点击“新增靶机”开始配置"/>
    </NSpace>
  </div>
</template>

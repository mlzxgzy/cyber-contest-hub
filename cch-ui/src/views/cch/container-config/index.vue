<script setup lang="tsx">
import {ref, computed, onMounted, watch} from 'vue';
import {
  fetchTestConnection,
  fetchGetContainerConfigList,
  fetchCreateContainerConfig,
  fetchUpdateContainerConfig,
  fetchGetActiveInstance,
  fetchDisconnect,
  fetchGetContainerList,
  fetchGetImageList
} from '@/service/api/cch/container-config';
import {useAuth} from '@/hooks/business/auth';
import {useLoading} from '@sa/hooks';
import SvgIcon from '@/components/custom/svg-icon.vue';

defineOptions({
  name: 'ContainerConfigList'
});

// 视图状态: 'select' | 'docker-form' | 'loading' | 'connected' | 'error' | 'checking'
type ViewState = 'select' | 'docker-form' | 'loading' | 'connected' | 'error' | 'checking';

const {hasAuth} = useAuth();
const {loading, startLoading, endLoading} = useLoading();

// 当前视图状态
const currentView = ref<ViewState>('checking');

// Docker连接表单
const dockerForm = ref({
  configName: '',
  dockerUrl: '',
  dockerApiVersion: '1.41',
  dockerCertPath: '',
  dockerTlsVerify: '0' as '0' | '1'
});

// 连接错误信息
const errorMessage = ref('');

// 当前连接的配置信息
const currentConfig = ref<Api.Cch.ContainerConfig | null>(null);

// 是否已连接
const isConnected = computed(() => currentView.value === 'connected');

// 容器和镜像数据
const containers = ref<Api.Cch.DockerContainer[]>([]);
const images = ref<Api.Cch.DockerImage[]>([]);
const containersLoading = ref(false);
const imagesLoading = ref(false);

// 页面加载时检查活跃实例
onMounted(async () => {
  try {
    const {data, error} = await fetchGetActiveInstance();
    if (!error && data) {
      currentConfig.value = data;
      currentView.value = 'connected';
    } else {
      currentView.value = 'select';
    }
  } catch {
    currentView.value = 'select';
  }
});

// 选择Docker
function selectDocker() {
  currentView.value = 'docker-form';
  errorMessage.value = '';
}

// 返回选择页面
function goBack() {
  currentView.value = 'select';
  dockerForm.value = {
    configName: '',
    dockerUrl: '',
    dockerApiVersion: '1.41',
    dockerCertPath: '',
    dockerTlsVerify: '0'
  };
  errorMessage.value = '';
}

// 连接Docker
async function connectDocker() {
  if (!dockerForm.value.dockerUrl) {
    window.$message?.error('请输入Docker URL');
    return;
  }

  const configName = dockerForm.value.configName || 'Docker';
  startLoading();
  currentView.value = 'loading';
  errorMessage.value = '';

  try {
    // 先查询是否已存在同名配置
    const listRes = await fetchGetContainerConfigList({pageNum: 1, pageSize: 100});
    const configs = listRes.data?.rows || [];
    const existingConfig = configs.find(
      (c: Api.Cch.ContainerConfig) => c.configName === configName
    );

    let targetConfig: Api.Cch.ContainerConfig | null = null;

    if (existingConfig) {
      // 配置名称已存在，更新配置
      const {error: updateError} = await fetchUpdateContainerConfig({
        id: existingConfig.id,
        configName,
        backendType: 'docker',
        dockerUrl: dockerForm.value.dockerUrl,
        dockerApiVersion: dockerForm.value.dockerApiVersion,
        dockerCertPath: dockerForm.value.dockerCertPath,
        dockerTlsVerify: dockerForm.value.dockerTlsVerify,
        status: '0'
      });

      if (updateError) {
        throw new Error('更新配置失败');
      }
      targetConfig = {...existingConfig, dockerUrl: dockerForm.value.dockerUrl};
      window.$message?.success('配置已更新');
    } else {
      // 配置名称不存在，创建新配置
      const {error: createError} = await fetchCreateContainerConfig({
        configName,
        backendType: 'docker',
        dockerUrl: dockerForm.value.dockerUrl,
        dockerApiVersion: dockerForm.value.dockerApiVersion,
        dockerCertPath: dockerForm.value.dockerCertPath,
        dockerTlsVerify: dockerForm.value.dockerTlsVerify,
        status: '0'
      });

      if (createError) {
        throw new Error('创建配置失败');
      }

      // 获取刚创建的配置
      const newListRes = await fetchGetContainerConfigList({pageNum: 1, pageSize: 100});
      const newConfigs = newListRes.data?.rows || [];
      targetConfig = newConfigs.find(
        (c: Api.Cch.ContainerConfig) => c.configName === configName
      ) || null;
    }

    if (!targetConfig) {
      throw new Error('未找到配置');
    }

    // 测试连接并激活实例
    const {error: testError} = await fetchTestConnection(targetConfig.id!);
    if (testError) {
      throw new Error('连接测试失败');
    }

    currentConfig.value = targetConfig;
    currentView.value = 'connected';
    window.$message?.success('Docker连接成功');
  } catch (err: any) {
    errorMessage.value = err.message || '连接失败，请检查配置';
    currentView.value = 'error';
  } finally {
    endLoading();
  }
}

// 重新尝试连接（从错误页面）
function retryConnect() {
  currentView.value = 'docker-form';
  errorMessage.value = '';
}

// 断开连接
async function disconnect() {
  const {error} = await fetchDisconnect();
  if (error) {
    window.$message?.error('断开连接失败');
    return;
  }
  currentConfig.value = null;
  currentView.value = 'select';
  window.$message?.success('已断开连接');
}

// 加载容器和镜像列表
async function loadContainerAndImageList() {
  if (!isConnected.value) return;

  containersLoading.value = true;
  imagesLoading.value = true;

  try {
    // 并行加载容器和镜像列表
    const [containerResponse, imageResponse] = await Promise.all([
      fetchGetContainerList(),
      fetchGetImageList()
    ]);

    if (!containerResponse.error) {
      containers.value = containerResponse.data || [];
    } else {
      window.$message?.error('获取容器列表失败');
    }

    if (!imageResponse.error) {
      images.value = imageResponse.data || [];
    } else {
      window.$message?.error('获取镜像列表失败');
    }
  } catch (err) {
    console.error('加载容器和镜像列表失败:', err);
    window.$message?.error('加载容器和镜像列表失败');
  } finally {
    containersLoading.value = false;
    imagesLoading.value = false;
  }
}

// 在连接成功后自动加载容器和镜像列表
watch(isConnected, (connected) => {
  if (connected) {
    loadContainerAndImageList();
  }
});
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="容器管理" :bordered="false" size="small" class="card-wrapper sm:flex-1-hidden">
      <!-- 检查连接状态 -->
      <template v-if="currentView === 'checking'">
        <div class="flex flex-col items-center justify-center gap-16px py-60px">
          <NSpin size="large"/>
          <span class="text-16px text-gray-500">正在检查连接状态...</span>
        </div>
      </template>

      <!-- 选择后端类型页面 -->
      <template v-else-if="currentView === 'select'">
        <div class="flex flex-col items-center justify-center gap-24px py-40px">
          <div class="text-18px font-medium text-gray-500">请选择容器后端类型</div>
          <NSpace :size="32">
            <!-- Docker按钮 -->
            <NButton
              type="primary"
              size="large"
              class="w-160px h-100px flex-col!"
              @click="selectDocker"
            >
              <template #icon>
                <SvgIcon icon="mdi:docker" class="text-40px"/>
              </template>
              <span class="mt-8px text-16px">Docker</span>
            </NButton>

            <!-- Kubernetes按钮（暂时禁用） -->
            <NButton
              size="large"
              class="w-160px h-100px flex-col! cursor-not-allowed opacity-60"
              disabled
            >
              <template #icon>
                <SvgIcon icon="mdi:kubernetes" class="text-40px"/>
              </template>
              <span class="mt-8px text-16px">Kubernetes</span>
              <span class="text-12px text-gray-400 mt-4px">(暂未开放)</span>
            </NButton>
          </NSpace>
        </div>
      </template>

      <!-- Docker连接表单页面 -->
      <template v-else-if="currentView === 'docker-form'">
        <div class="flex items-center gap-8px mb-16px">
          <NButton quaternary circle size="small" @click="goBack">
            <template #icon>
              <icon-ic-round-arrow-back class="text-icon"/>
            </template>
          </NButton>
          <span class="font-medium">Docker连接配置</span>
        </div>

        <NForm label-placement="left" label-width="120px" :model="dockerForm">
          <NFormItem label="配置名称" path="configName">
            <NInput v-model:value="dockerForm.configName" placeholder="给这个配置起个名字（可选）"/>
          </NFormItem>

          <NFormItem label="Docker URL" path="dockerUrl" required>
            <NInput
              v-model:value="dockerForm.dockerUrl"
              placeholder="例如: tcp://localhost:2375 或 unix:///var/run/docker.sock"
            />
          </NFormItem>

          <NFormItem label="API版本" path="dockerApiVersion">
            <NInput v-model:value="dockerForm.dockerApiVersion" placeholder="例如: 1.41"/>
          </NFormItem>

          <NFormItem label="TLS证书路径" path="dockerCertPath">
            <NInput v-model:value="dockerForm.dockerCertPath" placeholder="TLS证书路径（可选）"/>
          </NFormItem>

          <NFormItem label="TLS验证" path="dockerTlsVerify">
            <NRadioGroup v-model:value="dockerForm.dockerTlsVerify">
              <NRadio value="0">关闭</NRadio>
              <NRadio value="1">开启</NRadio>
            </NRadioGroup>
          </NFormItem>
        </NForm>

        <div class="flex justify-end gap-12px mt-24px">
          <NButton @click="goBack">返回</NButton>
          <NButton type="primary" @click="connectDocker">连接</NButton>
        </div>
      </template>

      <!-- Loading页面 -->
      <template v-else-if="currentView === 'loading'">
        <div class="flex flex-col items-center justify-center gap-16px py-60px">
          <NSpin size="large"/>
          <span class="text-16px text-gray-500">正在连接Docker服务器...</span>
          <span class="text-14px text-gray-400">{{ dockerForm.dockerUrl }}</span>
        </div>
      </template>

      <!-- 错误页面 -->
      <template v-else-if="currentView === 'error'">
        <div class="flex flex-col items-center justify-center gap-16px py-40px">
          <SvgIcon icon="mdi:alert-circle-outline" class="text-60px text-error"/>
          <span class="text-18px text-error">连接失败</span>
          <span class="text-14px text-gray-500">{{ errorMessage }}</span>
          <NSpace class="mt-16px">
            <NButton @click="goBack">返回</NButton>
            <NButton type="primary" @click="retryConnect">重新尝试</NButton>
          </NSpace>
        </div>
      </template>

      <!-- 已连接页面 -->
      <template v-else-if="currentView === 'connected'">
        <div class="flex flex-col gap-16px py-16px w-full">
          <div class="flex items-center gap-8px">
            <SvgIcon icon="mdi:check-circle-outline" class="text-24px text-success"/>
            <span class="text-18px text-success">Docker连接成功</span>
          </div>

          <div class="grid grid-cols-2 gap-16px">
            <!-- 左侧：当前连接信息 -->
            <div class="col-span-1">
              <NCard title="连接信息" :bordered="false" size="small" class="card-wrapper">
                <NDescriptions :column="1" label-placement="left">
                  <NDescriptionsItem label="配置名称">
                    {{ currentConfig?.configName || '-' }}
                  </NDescriptionsItem>
                  <NDescriptionsItem label="Docker URL">
                    {{ currentConfig?.dockerUrl || '-' }}
                  </NDescriptionsItem>
                  <NDescriptionsItem label="API版本">
                    {{ currentConfig?.dockerApiVersion || '-' }}
                  </NDescriptionsItem>
                  <NDescriptionsItem label="状态">
                    <NTag type="success" size="small">已连接</NTag>
                  </NDescriptionsItem>
                </NDescriptions>
                <div class="mt-16px flex justify-center">
                  <NButton type="error" @click="disconnect">
                    <template #icon>
                      <icon-ic-round-link-off class="text-icon"/>
                    </template>
                    断开连接
                  </NButton>
                </div>
              </NCard>
            </div>

            <!-- 右侧：容器和镜像信息 -->
            <div class="col-span-1">
              <!-- 容器列表 -->
              <NCard title="现有容器" :bordered="false" size="small" class="card-wrapper mb-16px">
                <NScrollbar style="max-height: 300px;">
                  <div v-if="containersLoading" class="py-20px text-center">
                    <NSpin size="small"/>
                  </div>
                  <div v-else-if="containers.length === 0" class="py-20px text-center text-gray-500">
                    暂无容器
                  </div>
                  <NList v-else>
                    <NListItem v-for="container in containers" :key="container.id">
                      <div class="flex flex-col">
                        <div class="font-bold">{{ container.names || container.id.substring(0, 12) }}</div>
                        <div class="text-sm text-gray-500 truncate">镜像: {{ container.image }}</div>
                        <div class="text-xs text-gray-400">端口: {{ container.ports || '未映射' }}</div>
                        <div class="text-xs">
                          <NTag :type="container.status.includes('Up') ? 'success' : 'warning'" size="tiny">
                            {{ container.status }}
                          </NTag>
                        </div>
                      </div>
                    </NListItem>
                  </NList>
                </NScrollbar>
              </NCard>

              <!-- 镜像列表 -->
              <NCard title="现有镜像" :bordered="false" size="small" class="card-wrapper">
                <NScrollbar style="max-height: 300px;">
                  <div v-if="imagesLoading" class="py-20px text-center">
                    <NSpin size="small"/>
                  </div>
                  <div v-else-if="images.length === 0" class="py-20px text-center text-gray-500">
                    暂无镜像
                  </div>
                  <NList v-else>
                    <NListItem v-for="image in images" :key="image.id">
                      <div class="flex flex-col">
                        <div class="font-bold">{{ image.repoTags || image.shortId }}</div>
                        <div class="text-sm text-gray-500 truncate">仓库: {{ image.repository }}</div>
                        <div class="text-xs text-gray-400">大小: {{ image.sizeHuman }}</div>
                        <div class="text-xs text-gray-400">标签: {{ image.tag }}</div>
                      </div>
                    </NListItem>
                  </NList>
                </NScrollbar>
              </NCard>
            </div>
          </div>
        </div>
      </template>
    </NCard>
  </div>
</template>

<style scoped></style>

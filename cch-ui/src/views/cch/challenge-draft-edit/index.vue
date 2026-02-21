<script lang="ts" setup>
import {computed, nextTick, onMounted, ref, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {
  NButton,
  NCard,
  NEmpty,
  NGrid,
  NGi,
  NInput,
  NInputNumber,
  NRadioButton,
  NRadioGroup,
  NSelect,
  NSpace,
  NTag,
  NTabs,
  NTabPane,
  NSkeleton
} from 'naive-ui';
import {fetchChallengeDraftByChallengeId, fetchGetChallengeById} from '@/service/api/cch/challenge';
import {
  fetchGetChallengeDraftById,
  fetchUpdateChallengeDraft
} from '@/service/api/cch/challenge-draft';
import {useTabStore} from '@/store/modules/tab';
import {getRoutePath} from '@/router/elegant/transform';
import ChallengeDraftHistory from './modules/challenge-draft-edit-history.vue';
import ChallengeImageManagement from "@/views/cch/challenge-draft-edit/modules/challenge-image-management.vue";
import ContainerTargetConfig from "@/views/cch/challenge-draft-edit/modules/container-target-config.vue";
import ChallengeBasicInfo from "@/views/cch/challenge-draft-edit/modules/challenge-basic-info.vue";
import ChallengeContainerMockTest from "@/views/cch/challenge-draft-edit/modules/challenge-container-mock-test.vue";
import ChallengeVersionPublishModal from "@/views/cch/challenge-draft-edit/modules/challenge-version-publish-modal.vue";
import {useTabQuerySync} from './useTabQuerySync';

defineOptions({
  name: 'ChallengeDraftEdit'
});

const route = useRoute();
const router = useRouter();
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
const split = ref(0.75);

// Tab状态同步到URL（刷新后可定位）
const {activeMainTab, activeSideTab} = useTabQuerySync({route, router, draftData});

// 是否为派生模式（从历史版本派生，保存时新增版本）
const isForkMode = computed(() => !!route.query.forkFrom);

// 派生的父草稿ID
const forkFromDraftId = computed(() => parseQueryId(route.query.forkFrom));

// 历史组件引用
const historyRef = ref<InstanceType<typeof ChallengeDraftHistory> | null>(null);

// 发版对话框显示状态
const publishModalVisible = ref(false);

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
  draftData.value.config.containerTargets ??= [];
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
  const forkFrom = queryParams.forkFrom;

  console.log('[loadData] 开始加载', {currentChallengeId, currentDraftId, forkFrom, isForkMode: isForkMode.value});

  if (currentDraftId) {
    await loadDraftDataById(currentDraftId);
    console.log('[loadData] 加载草稿后', {
      draftData: draftData.value?.id,
      challengeId: draftData.value?.challengeId,
      currentChallengeId
    });
    // 如果草稿中有 challengeId，优先使用；否则使用 URL 中的 challengeId
    const effectiveChallengeId = draftData.value?.challengeId || currentChallengeId;
    if (effectiveChallengeId) {
      challengeId.value = effectiveChallengeId;
      await loadChallengeData(effectiveChallengeId);
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
    console.log('[loadData] 刷新历史列表', {challengeId: challengeId.value, draftId: draftId.value});
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
    // 容器靶机配置基础校验
    if (draftData.value.config?.runType === 'container') {
      const targets = draftData.value.config.containerTargets ?? [];
      for (let i = 0; i < targets.length; i += 1) {
        const t = targets[i];
        if (!t) continue;
        // 校验靶机名称必填
        if (!t.name || !t.name.trim()) {
          window.$message?.error(`靶机 ${i + 1}：名称不能为空`);
          return;
        }
        if (!t.imageId) {
          window.$message?.error(`靶机 ${i + 1}：请选择镜像`);
          return;
        }
        const ports = t.ports ?? {};
        for (const [portName, cfg] of Object.entries(ports)) {
          const name = (portName || '').trim();
          if (!name) continue;
          const protocol = (cfg?.protocol || '').trim();
          const internalPort = cfg?.internalPort;
          const externalPort = cfg?.externalPort;
          if (!protocol) {
            window.$message?.error(`靶机 ${i + 1}：端口「${name}」协议不能为空`);
            return;
          }
          if (!internalPort || internalPort <= 0) {
            window.$message?.error(`靶机 ${i + 1}：端口「${name}」内部端口必须为正整数`);
            return;
          }
        }
      }
    }

    const requestData: Api.Cch.ChallengeDraftOperateParams = {
      id: draftData.value.id,
      challengeId: draftData.value.challengeId,
      challengeName: challengeData.value.name,
      // 由后端负责同步更新 Challenge.remark；这里单独传递，避免前端调用额外接口
      challengeRemark: challengeData.value.remark,
      challengeCategory: challengeData.value.category,
      challengeDescription: draftData.value.challengeDescription,
      config: draftData.value.config
    };

    // 派生模式下保存，需要新增版本（设置 operateType 为非 edit）
    if (isForkMode.value) {
      requestData.operateType = 'save';
    }

    const {data, error} = await fetchUpdateChallengeDraft(requestData);

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
          draftId: data.id,
          tab: activeMainTab.value,
          side: activeSideTab.value
        }
      });

      // 使用 nextTick 确保 draftId 和 challengeId 都更新后再刷新历史
      await nextTick();
      console.log('[saveDraft] 刷新历史', {draftId: draftId.value, challengeId: challengeId.value});

      // 重新加载版本历史，并确保显示当前版本
      if (historyRef.value) {
        await historyRef.value.refresh();
      }
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

function handlePublish() {
  if (!draftData.value || !challengeData.value.id) {
    window.$message?.warning('请先保存草稿');
    return;
  }
  publishModalVisible.value = true;
}

async function handlePublishSubmitted() {
  // 发版成功后刷新历史记录
  if (historyRef.value) {
    await historyRef.value.refresh();
  }
}

// 加载镜像列表
async function loadImageList() {
  // 保留此函数以供子组件事件调用
}
</script>

<template>
  <div class="draft-container">
    <!-- 顶部标题栏 -->
    <div class="draft-header">
      <div class="header-left">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L3 7V12C3 17.55 6.84 22.74 12 24C17.16 22.74 21 17.55 21 12V7L12 2Z" fill="currentColor"
                  opacity="0.15"/>
            <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                  stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="header-content">
          <h1 class="header-title">题目草稿编辑</h1>
          <span v-if="isForkMode" class="header-badge">派生编辑</span>
        </div>
      </div>
      <div class="header-actions">
        <NButton :loading="saving" :disabled="!hasEdited || saving" type="primary" size="large" @click="saveDraft">
          <template #icon>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
              <polyline points="17 21 17 13 7 13 7 21"/>
              <polyline points="7 3 7 8 15 8"/>
            </svg>
          </template>
          保存草稿
        </NButton>
        <NButton
          :disabled="!draftData || !challengeData.id"
          type="info"
          size="large"
          @click="handlePublish"
        >
          <template #icon>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </template>
          发版
        </NButton>
        <NButton size="large" @click="goBack">
          <template #icon>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="19" y1="12" x2="5" y2="12"/>
              <polyline points="12 19 5 12 12 5"/>
            </svg>
          </template>
          返回
        </NButton>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="draft-main">
      <NSplit v-model:size="split" direction="horizontal" class="draft-split">
        <template #1>
          <div class="main-panel">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-wrapper">
              <NCard class="loading-card">
                <div class="skeleton-grid">
                  <NSkeleton text :repeat="3" class="skeleton-item"/>
                  <NSkeleton text :repeat="2" class="skeleton-item"/>
                  <NSkeleton text :repeat="4" class="skeleton-item"/>
                </div>
              </NCard>
            </div>

            <!-- 数据加载完成 -->
            <template v-else-if="draftData">
              <!-- 标签页导航 -->
              <div class="tab-nav-wrapper">
                <NTabs v-model:value="activeMainTab" type="card" animated class="cyber-tabs">
                  <NTabPane name="info" tab="题目信息" tab-class="cyber-tab">
                    <template #tab>
                      <span class="tab-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                          <polyline points="14 2 14 8 20 8"/>
                          <line x1="16" y1="13" x2="8" y2="13"/>
                          <line x1="16" y1="17" x2="8" y2="17"/>
                        </svg>
                      </span>
                      题目信息
                    </template>
                    <div class="pane-content">
                      <ChallengeBasicInfo :challenge-data="challengeData" :draft-data="draftData"/>
                    </div>
                  </NTabPane>

                  <NTabPane name="flag" tab="Flag管理" tab-class="cyber-tab">
                    <template #tab>
                      <span class="tab-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/>
                          <line x1="4" y1="22" x2="4" y2="15"/>
                        </svg>
                      </span>
                      Flag管理
                    </template>
                    <div class="pane-content">
                      <div class="flag-header">
                        <h3 class="section-title">Flag 列表</h3>
                        <NSpace>
                          <NButton type="primary" @click="addFlag('static')">
                            <template #icon>
                              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <line x1="12" y1="5" x2="12" y2="19"/>
                                <line x1="5" y1="12" x2="19" y2="12"/>
                              </svg>
                            </template>
                            静态Flag
                          </NButton>
                          <NButton @click="addFlag('dynamic')">
                            <template #icon>
                              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                                <path d="M2 17l10 5 10-5"/>
                                <path d="M2 12l10 5 10-5"/>
                              </svg>
                            </template>
                            动态Flag
                          </NButton>
                        </NSpace>
                      </div>

                      <div v-if="draftData.config.flags?.length" class="flag-list">
                        <div v-for="(flag, index) of draftData.config.flags" :key="index" class="flag-item">
                          <div class="flag-header-row">
                            <div class="flag-title-row">
                              <NTag :type="flag.type === 'static' ? 'success' : 'warning'" size="small">
                                {{ getFlagTypeLabel(flag.type) }}
                              </NTag>
                              <span class="flag-index">Flag {{ index + 1 }}</span>
                            </div>
                            <NButton text type="error" size="small" @click="removeFlag(index)" class="delete-btn">
                              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <polyline points="3 6 5 6 21 6"/>
                                <path
                                  d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                              </svg>
                              删除
                            </NButton>
                          </div>
                          <div class="flag-body">
                            <NGrid cols="1 800:2" x-gap="12" y-gap="12">
                              <NGi>
                                <div class="form-group">
                                  <label class="form-label">Flag类型</label>
                                  <NSelect
                                    :value="flag.type"
                                    :options="[
                                      { label: '静态', value: 'static' },
                                      { label: '动态', value: 'dynamic' }
                                    ]"
                                    disabled
                                    class="cyber-input"
                                  />
                                </div>
                              </NGi>
                              <NGi>
                                <div class="form-group">
                                  <label class="form-label">分值（推荐）</label>
                                  <NInputNumber
                                    v-model:value="flag.score"
                                    :min="0"
                                    :precision="0"
                                    placeholder="请输入分值"
                                    class="cyber-input w-full"
                                  />
                                </div>
                              </NGi>
                              <NGi v-if="flag.type === 'static'">
                                <div class="form-group">
                                  <label class="form-label">Flag内容</label>
                                  <NInput
                                    v-model:value="(flag as Api.Cch.ChallengeDraftConfigStaticFlag).content"
                                    placeholder="请输入Flag内容"
                                    type="textarea"
                                    :rows="2"
                                    class="cyber-input"
                                  />
                                </div>
                              </NGi>
                              <NGi v-if="flag.type === 'dynamic'">
                                <div class="form-group">
                                  <label class="form-label">生成规则配置</label>
                                  <NInput
                                    v-model:value="(flag as Api.Cch.ChallengeDraftConfigDynamicFlag).generatorConfig"
                                    placeholder="动态Flag生成规则（待实现）"
                                    type="textarea"
                                    :rows="2"
                                    disabled
                                    class="cyber-input"
                                  />
                                </div>
                              </NGi>
                              <NGi>
                                <div class="form-group">
                                  <label class="form-label">Flag描述（给选手查看）</label>
                                  <NInput
                                    v-model:value="flag.description"
                                    placeholder="请输入Flag描述，此内容会展示给选手"
                                    type="textarea"
                                    :rows="2"
                                    class="cyber-input"
                                  />
                                </div>
                              </NGi>
                              <NGi>
                                <div class="form-group">
                                  <label class="form-label">Flag备注（仅后台可见）</label>
                                  <NInput
                                    v-model:value="flag.remark"
                                    placeholder="请输入Flag备注，仅后台管理员可见"
                                    type="textarea"
                                    :rows="2"
                                    class="cyber-input"
                                  />
                                </div>
                              </NGi>
                            </NGrid>
                          </div>
                        </div>
                      </div>
                      <NEmpty v-else description="暂无Flag，点击上方按钮添加" class="empty-state"/>
                    </div>
                  </NTabPane>

                  <NTabPane v-if="draftData?.config?.runType === 'container'" name="container" tab-class="cyber-tab">
                    <template #tab>
                      <span class="tab-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
                          <line x1="8" y1="21" x2="16" y2="21"/>
                          <line x1="12" y1="17" x2="12" y2="21"/>
                        </svg>
                      </span>
                      容器镜像
                    </template>
                    <div class="pane-content">
                      <ChallengeImageManagement
                        :challenge-id="challengeId"
                        @update="loadImageList"
                      />
                    </div>
                  </NTabPane>

                  <NTabPane v-if="draftData?.config?.runType === 'container'" name="container-target"
                            tab-class="cyber-tab">
                    <template #tab>
                      <span class="tab-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <circle cx="12" cy="12" r="10"/>
                          <line x1="2" y1="12" x2="22" y2="12"/>
                          <path
                            d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                        </svg>
                      </span>
                      容器靶机
                    </template>
                    <div class="pane-content">
                      <ContainerTargetConfig 
                        v-model="draftData.config.containerTargets" 
                        :challenge-id="challengeId"
                        :challenge-name="challengeData.name"
                      />
                    </div>
                  </NTabPane>

                  <NTabPane v-if="draftData?.config?.runType === 'vm'" name="vm" tab-class="cyber-tab">
                    <template #tab>
                      <span class="tab-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
                          <line x1="8" y1="21" x2="16" y2="21"/>
                          <line x1="12" y1="17" x2="12" y2="21"/>
                        </svg>
                      </span>
                      虚拟机
                    </template>
                    <div class="pane-content">
                      <div class="vm-placeholder">
                        <div class="placeholder-icon">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
                            <line x1="8" y1="21" x2="16" y2="21"/>
                            <line x1="12" y1="17" x2="12" y2="21"/>
                          </svg>
                        </div>
                        <h3>虚拟机管理</h3>
                        <p>功能开发中...</p>
                      </div>
                    </div>
                  </NTabPane>
                </NTabs>
              </div>
            </template>

            <!-- 加载失败状态 -->
            <NCard v-else class="error-card">
              <NEmpty description="未能加载草稿数据">
                <template #extra>
                  <NButton type="primary" @click="goBack">返回列表</NButton>
                </template>
              </NEmpty>
            </NCard>
          </div>
        </template>

        <template #2>
          <div class="side-panel">
            <NCard class="side-card">
              <NTabs v-model:value="activeSideTab" type="line" animated>
                <NTabPane name="history" tab="修改历史">
                  <ChallengeDraftHistory
                    ref="historyRef"
                    :challenge-id="challengeId"
                    :current-draft-id="draftId"
                    :fork-from="forkFromDraftId"
                  />
                </NTabPane>
                <NTabPane name="containerMockTest" tab="容器模拟测试">
                  <ChallengeContainerMockTest :current-draft-id="draftId" :challenge-id="challengeId"/>
                </NTabPane>
              </NTabs>
            </NCard>
          </div>
        </template>
      </NSplit>
    </div>

    <!-- 发版对话框 -->
    <ChallengeVersionPublishModal
      v-model:visible="publishModalVisible"
      :challenge-id="challengeData.id"
      :challenge-name="challengeData.name"
      :draft-id="draftId"
      @submitted="handlePublishSubmitted"
    />
  </div>
</template>

<style scoped lang="scss">
// 变量定义
$primary-color: #3b82f6;
$primary-hover: #2563eb;
$primary-light: rgba(59, 130, 246, 0.1);
$success-color: #10b981;
$success-light: rgba(16, 185, 129, 0.1);
$warning-color: #f59e0b;
$warning-light: rgba(245, 158, 11, 0.1);
$danger-color: #ef4444;
$danger-light: rgba(239, 68, 68, 0.1);
$info-color: #06b6d4;
$info-light: rgba(6, 182, 212, 0.1);

$border-color: #e5e7eb;
$border-radius-sm: 2px;
$border-radius: 4px;
$shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
$shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
$shadow-lg: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);

$text-primary: #1f2937;
$text-secondary: #6b7280;
$text-muted: #9ca3af;

$bg-primary: #ffffff;
$bg-secondary: #f9fafb;
$bg-tertiary: #f3f4f6;
$bg-hover: #f3f4f6;

// 容器样式
.draft-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: $bg-secondary;
  min-height: 0; // 关键：允许内部滚动容器正确计算高度
}

// 顶部标题栏
.draft-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: $bg-primary;
  border-bottom: 1px solid $border-color;
  box-shadow: $shadow-sm;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .header-icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $primary-color;
    border-radius: $border-radius-sm;
    color: white;

    svg {
      width: 24px;
      height: 24px;
    }
  }

  .header-content {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .header-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: $text-primary;
  }

  .header-badge {
    padding: 4px 12px;
    background: $warning-color;
    color: white;
    font-size: 12px;
    font-weight: 500;
    border-radius: $border-radius-sm;
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

// 主内容区域
.draft-main {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  min-height: 0; // 关键：flex 子项默认 min-height:auto 会导致无法滚动
}

.draft-split {
  height: 100%;

  :deep(.n-split-wrapper) {
    height: 100%;
  }

  :deep(.n-split-pane) {
    height: 100%;
    min-height: 0; // 关键：让 pane 内的 overflow 生效
  }
}

// 主面板
.main-panel {
  height: 100%;
  min-height: 0;
  overflow: auto;
  display: flex;
  flex-direction: column;
  padding-right: 8px;
}

// 标签页容器
.tab-nav-wrapper {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

// 扁平化标签页
.cyber-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;

  :deep(.n-tabs-nav) {
    background: $bg-primary;
    border-radius: $border-radius-sm;
    padding: 6px 6px 0 6px;
    box-shadow: $shadow-sm;
    flex-shrink: 0;
    border-bottom: 1px solid $border-color;
  }

  :deep(.n-tabs-tab-wrapper) {
    margin-right: 2px;
  }

  :deep(.n-tabs-tab) {
    padding: 10px 18px;
    border-radius: $border-radius-sm $border-radius-sm 0 0;
    background: $bg-secondary;
    color: $text-secondary;
    font-weight: 500;
    font-size: 13px;
    transition: all 0.2s ease;
    border: 1px solid transparent;
    border-bottom: none;

    &:hover {
      color: $primary-color;
      background: $bg-hover;
    }

    &.n-tabs-tab--active {
      color: $primary-color;
      background: $bg-primary;
      border-color: $border-color;
      border-bottom-color: $bg-primary;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: -1px;
        left: 0;
        right: 0;
        height: 2px;
        background: $primary-color;
      }
    }
  }

  // 标签内容区域 - 关键：让这个区域可以滚动
  :deep(.n-tabs-content) {
    flex: 1;
    overflow: hidden;
    min-height: 0;
  }

  :deep(.n-tabs-content--animated) {
    height: 100%;
  }

  // NaiveUI 实际的 pane 容器：n-tabs-pane-wrapper / n-tab-pane
  // 让 wrapper 占满剩余高度，并把滚动交给真正的 pane
  :deep(.n-tabs-pane-wrapper) {
    flex: 1;
    height: 100%;
    overflow: hidden;
    min-height: 0;
  }

  :deep(.n-tab-pane) {
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    min-height: 0;
  }

  :deep(.n-tabs-tab-pane) {
    padding: 0;
    height: 100%;
  }
}

// 加载状态
.loading-wrapper {
  padding: 24px;
}

.loading-card {
  border-radius: $border-radius;
  border: 1px solid $border-color;
  box-shadow: $shadow-sm;
}

.skeleton-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// 标签页导航
.tab-nav-wrapper {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tab-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 6px;

  svg {
    width: 16px;
    height: 16px;
  }
}

// 内容面板
.pane-content {
  padding: 16px;
  height: 100%;
  box-sizing: border-box;
}

// 信息卡片（扁平化）
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
    color: white;
    flex-shrink: 0;

    svg {
      width: 18px;
      height: 18px;
    }

    &.basic {
      background: $primary-color;
    }

    &.config {
      background: $info-color;
    }

    &.stem {
      background: #8b5cf6;
    }

    &.attachment {
      background: $success-color;
    }

    &.writeup {
      background: $warning-color;
    }
  }

  .card-title {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: $text-primary;
    letter-spacing: 0.01em;
  }

  .card-body {
    padding: 16px;
  }
}

// 表单样式
.form-group {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }

  &.full-height {
    height: calc(100% - 24px);
  }
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 500;
  color: $text-secondary;
}

.cyber-input {
  width: 100%;

  :deep(.n-input) {
    border-radius: $border-radius-sm;
    background: $bg-primary;
    border: 1px solid $border-color;
    transition: all 0.2s ease;

    &:hover {
      border-color: $primary-color;
    }

    &:focus {
      border-color: $primary-color;
      box-shadow: 0 0 0 2px $primary-light;
    }
  }
}

.cyber-select {
  width: 100%;

  :deep(.n-base-select) {
    border-radius: $border-radius-sm;
  }
}

.cyber-radio-group {
  display: flex;
  gap: 8px;

  :deep(.n-radio-button) {
    border-radius: $border-radius-sm;
    padding: 6px 16px;
    background: $bg-secondary;
    border: 1px solid $border-color;
    color: $text-secondary;
    transition: all 0.2s ease;

    &:hover {
      border-color: $primary-color;
      color: $primary-color;
    }

    &.n-radio-button--checked {
      background: $primary-color;
      border-color: $primary-color;
      color: white;
    }
  }
}

// 文件列表
.file-list {
  margin-top: 16px;
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
    width: 18px;
    height: 18px;
  }

  &.attachment {
    background: $success-light;
    color: $success-color;
  }

  &.writeup {
    background: rgba(245, 158, 11, 0.1);
    color: $warning-color;
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
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-remark {
  :deep(.n-input) {
    background: transparent;
    border: none;
    font-size: 12px;

    .n-input__input-el {
      text-overflow: ellipsis;
    }
  }
}

.file-action {
  flex-shrink: 0;

  svg {
    width: 18px;
    height: 18px;
  }
}

// Flag管理
.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
}

.flag-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.flag-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.flag-item {
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
}

.flag-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: $bg-secondary;
  border-bottom: 1px solid $border-color;
}

.flag-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.flag-index {
  font-size: 14px;
  font-weight: 500;
  color: $text-primary;
}

.delete-btn {
  display: flex;
  align-items: center;
  gap: 4px;

  svg {
    width: 16px;
    height: 16px;
  }
}

.flag-body {
  padding: 16px;
}

// 空状态
.empty-state {
  padding: 48px 0;
}

// 错误卡片
.error-card {
  border-radius: $border-radius;
  border: 1px solid $border-color;
  box-shadow: $shadow-sm;

  :deep(.n-card__content) {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 300px;
  }
}

// 虚拟机占位符
.vm-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 24px;
  background: $bg-primary;
  border-radius: $border-radius;
  border: 1px solid $border-color;
  box-shadow: $shadow-sm;
  text-align: center;

  .placeholder-icon {
    width: 80px;
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $bg-secondary;
    border-radius: 50%;
    margin-bottom: 24px;
    color: $text-muted;

    svg {
      width: 40px;
      height: 40px;
    }
  }

  h3 {
    margin: 0 0 8px 0;
    font-size: 18px;
    font-weight: 600;
    color: $text-primary;
  }

  p {
    margin: 0;
    font-size: 14px;
    color: $text-muted;
  }
}

// Oasis 内容
.oasis-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;

  .oasis-icon {
    width: 64px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $bg-secondary;
    border-radius: 50%;
    margin-bottom: 16px;
    color: $primary-color;

    svg {
      width: 32px;
      height: 32px;
    }
  }

  p {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
    color: $text-secondary;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .draft-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;

    .header-actions {
      width: 100%;
      justify-content: flex-end;
    }
  }

  .flag-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>

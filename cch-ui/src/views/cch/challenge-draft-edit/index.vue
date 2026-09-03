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
import {fetchChallengeDraftByChallengeId, fetchGetChallengeById, fetchInitChallenge} from '@/service/api/cch/challenge';
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
import ChallengePublishCheck from "@/views/cch/challenge-draft-edit/modules/challenge-publish-check.vue";
import ChallengeVersionPublishModal from "@/views/cch/challenge-draft-edit/modules/challenge-version-publish-modal.vue";
import ChallengeProjectAttach from "@/views/cch/challenge-draft-edit/modules/challenge-project-attach.vue";
import VmConfig from "@/views/cch/challenge-draft-edit/modules/vm-config.vue";
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
const {activeMainTab, activeSideTab} = useTabQuerySync({route, router, draftData, latestVersionId: computed(() => challengeData.value.latestVersionId)});

// 是否为派生模式（从历史版本派生，保存时新增版本）
const isForkMode = computed(() => !!route.query.forkFrom);

// 派生的父草稿ID
const forkFromDraftId = computed(() => parseQueryId(route.query.forkFrom));

// 历史组件引用
const historyRef = ref<InstanceType<typeof ChallengeDraftHistory> | null>(null);

// 发版对话框显示状态
const publishModalVisible = ref(false);

// 是否为"新增题目入库"创建模式（无 challengeId/draftId，第一步先创建题目+草稿）
const isCreateMode = computed(() => {
  return !parseQueryId(route.query.challengeId) && !parseQueryId(route.query.draftId) && !route.query.forkFrom;
});

// 入库步骤条定义（对应 CTF 入库流程：基本信息 -> 题目内容 -> Flag -> 环境 -> 入库）
const intakeSteps = computed(() => {
  const cfg = draftData.value?.config;
  const runType = cfg?.runType;
  const name = (challengeData.value?.name || '').trim();
  const category = challengeData.value?.category;
  const basicDone = !!name && !!category;
  const contentDone = !!(cfg?.stem || '').trim();
  const flags = cfg?.flags ?? [];
  const flagDone = flags.length > 0 && flags.every(f => {
    if (!f) return true;
    if ((f as Api.Cch.ChallengeDraftConfigStaticFlag).type === 'static') {
      return !!((f as Api.Cch.ChallengeDraftConfigStaticFlag).content || '').trim();
    }
    return true;
  });
  const containerTargets = cfg?.containerTargets ?? [];
  const envDone = runType === 'container'
    ? containerTargets.length > 0 && containerTargets.every(t => !!t && !!t.imageId)
    : true;
  const published = !!challengeData.value?.latestVersionId;
  return [
    {key: 'basic', title: '基本信息', desc: '分类 / 名称 / 难度', done: basicDone, tab: 'info'},
    {key: 'content', title: '题目内容', desc: '题干 / 附件 / Writeup', done: contentDone, tab: 'info'},
    {key: 'flag', title: 'Flag配置', desc: '静态 / 动态', done: flagDone, tab: 'flag'},
    {key: 'env', title: '环境配置', desc: runType === 'container' ? '镜像 / 靶机' : runType === 'vm' ? '虚拟机' : '静态题目', done: envDone, tab: runType === 'container' ? 'container-target' : runType === 'vm' ? 'vm' : 'info'},
    {key: 'publish', title: '检查与入库', desc: '完整性检查 / 发版', done: published, tab: 'publish'}
  ];
});

// 当前步骤（根据激活的tab反推，用于步骤条高亮）
const currentStepIndex = computed(() => {
  const idx = intakeSteps.value.findIndex(s => s.tab === activeMainTab.value);
  return idx === -1 ? 0 : idx;
});

// 点击步骤跳转到对应tab
function handleStepJump(index: number) {
  const step = intakeSteps.value[index];
  if (!step) return;
  activeMainTab.value = step.tab;
}

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
    // 无 challengeId/draftId：进入"新增题目入库"创建模式，第一步先创建题目+草稿
    enterCreateMode();
  }

  // 加载版本历史
  if (challengeId.value) {
    console.log('[loadData] 刷新历史列表', {challengeId: challengeId.value, draftId: draftId.value});
    historyRef.value?.refresh();
  }

  loading.value = false;
}

// 进入"新增题目入库"创建模式：初始化空壳数据，等待用户填写基本信息后创建
function enterCreateMode() {
  challengeInitialized.value = false;
  challengeData.value = {
    id: null,
    category: '',
    name: '',
    remark: '',
    latestVersionId: null,
    published: false,
    latestVersionTag: '',
    delFlag: 0
  } as unknown as Api.Cch.Challenge;
  challengeInitialized.value = true;

  const emptyDraft: Api.Cch.ChallengeDraft = {
    id: null,
    challengeId: null,
    challengeName: '',
    challengeDescription: '',
    config: {
      stem: '',
      difficulty: null,
      runType: 'static',
      knowledge: [],
      attachments: [],
      writeups: [],
      flags: [],
      containerTargets: []
    },
    delFlag: 0
  } as unknown as Api.Cch.ChallengeDraft;
  applyDraftData(emptyDraft);
  activeMainTab.value = 'info';
}

// 创建模式下：校验基本信息并创建题目+草稿（一个事务内完成），随后进入编辑模式继续完善
async function createChallengeAndEnter() {
  const name = (challengeData.value?.name || '').trim();
  const category = challengeData.value?.category;
  if (!name) {
    window.$message?.warning('请先填写题目名称');
    return;
  }
  if (!category) {
    window.$message?.warning('请先选择题目类型');
    return;
  }

  saving.value = true;
  try {
    const requestData: Api.Cch.ChallengeDraftOperateParams = {
      challengeName: name,
      challengeCategory: category,
      challengeRemark: challengeData.value.remark || '',
      challengeDescription: draftData.value?.challengeDescription,
      config: draftData.value?.config
    };
    const {data, error} = await fetchInitChallenge(requestData);
    if (error) {
      window.$message?.error(`创建题目失败: ${error}`);
      return;
    }
    if (!data) return;

    // 创建成功：应用草稿数据并切换到编辑模式（URL 携带 challengeId + draftId）
    applyDraftData(data);
    draftId.value = data.id;
    challengeId.value = data.challengeId;
    await loadChallengeData(data.challengeId);
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
    await nextTick();
    historyRef.value?.refresh();
    window.$message?.success('题目已创建，请继续完善内容后发版入库');
  } catch (err) {
    window.$message?.error(`创建题目异常: ${err}`);
  } finally {
    saving.value = false;
  }
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

  // 创建模式下"保存草稿"即"创建并进入编辑"
  if (isCreateMode.value) {
    await createChallengeAndEnter();
    return;
  }

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
  // 发版成功后刷新题目数据（更新入库状态徽标与步骤条）与历史记录
  if (challengeId.value) {
    await loadChallengeData(challengeId.value);
  }
  if (historyRef.value) {
    await historyRef.value.refresh();
  }
}

// 跳转到右侧"容器模拟测试"侧栏（从入库检查页发起）
function handleGoMockTest() {
  activeSideTab.value = 'containerMockTest';
}

// 加载镜像列表
async function loadImageList() {
  // 保留此函数以供子组件事件调用
}
</script>

<template>
  <div class="draft-container">
    <!-- 顶部标题栏（紧凑单行：标题 + 徽标 + 入库步骤 + 操作） -->
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
          <h1 class="header-title">{{ isCreateMode ? '新增题目入库' : '题目草稿编辑' }}</h1>
          <span v-if="isCreateMode" class="header-badge create-badge">新建</span>
          <span v-else-if="isForkMode" class="header-badge">派生编辑</span>
          <span v-if="!isCreateMode && draftData?.draftVersion" class="header-badge version-badge">第{{ draftData.draftVersion }}版</span>
          <span v-if="challengeData.latestVersionId" class="header-badge published-badge">已入库 {{ challengeData.latestVersionTag || '' }}</span>
        </div>
      </div>
      <!-- 入库流程步骤条（压缩为标题栏内胶囊组） -->
      <div v-if="!isCreateMode" class="hdr-steps">
        <template v-for="(step, index) in intakeSteps" :key="step.key">
          <div
            class="step"
            :class="{
              'is-done': step.done,
              'is-current': index === currentStepIndex && !step.done,
              'is-current-done': index === currentStepIndex && step.done
            }"
            @click="handleStepJump(index)"
          >
            <span class="step-num">
              <svg v-if="step.done" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              <template v-else>{{ index + 1 }}</template>
            </span>
            <span class="step-title">{{ step.title }}</span>
          </div>
          <span v-if="index < intakeSteps.length - 1" class="step-sep"></span>
        </template>
      </div>
      <div class="header-actions">
        <template v-if="isCreateMode">
          <NButton :loading="saving" type="primary" size="small" @click="createChallengeAndEnter">
            <template #icon>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
                <polyline points="17 21 17 13 7 13 7 21"/>
                <polyline points="7 3 7 8 15 8"/>
              </svg>
            </template>
            创建并保存草稿
          </NButton>
        </template>
        <template v-else>
          <NButton :loading="saving" :disabled="!hasEdited || saving" type="primary" size="small" @click="saveDraft">
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
            type="primary"
            ghost
            size="small"
            @click="handlePublish"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </template>
            发版
          </NButton>
        </template>
        <NButton size="small" @click="goBack">
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
                <NTabs v-model:value="activeMainTab" type="line" size="small" animated class="cyber-tabs">
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
                      <div v-if="isCreateMode" class="create-tip">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <circle cx="12" cy="12" r="10"/>
                          <line x1="12" y1="16" x2="12" y2="12"/>
                          <line x1="12" y1="8" x2="12.01" y2="8"/>
                        </svg>
                        <span>填写题目基本信息后点击右上角「创建并保存草稿」，系统将一次性创建题目与首个草稿，随后可继续完善内容并入库。</span>
                      </div>
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
                        <NSpace :size="8">
                          <NButton type="primary" size="small" @click="addFlag('static')">
                            <template #icon>
                              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <line x1="12" y1="5" x2="12" y2="19"/>
                                <line x1="5" y1="12" x2="19" y2="12"/>
                              </svg>
                            </template>
                            静态Flag
                          </NButton>
                          <NButton size="small" @click="addFlag('dynamic')">
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
                      <VmConfig v-model="draftData.config" />
                    </div>
                  </NTabPane>

                  <NTabPane name="publish" tab="入库检查" tab-class="cyber-tab">
                    <template #tab>
                      <span class="tab-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M9 12l2 2 4-4"/>
                          <path d="M12 2L3 7v5c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-9-5z"/>
                        </svg>
                      </span>
                      入库检查
                    </template>
                    <div class="pane-content">
                      <ChallengePublishCheck
                        :challenge-data="challengeData"
                        :draft-data="draftData"
                        :is-create-mode="isCreateMode"
                        @save="saveDraft"
                        @publish="handlePublish"
                        @mock-test="handleGoMockTest"
                      />
                    </div>
                  </NTabPane>

                  <NTabPane v-if="challengeData.latestVersionId" name="attach" tab="附加到项目" tab-class="cyber-tab">
                    <template #tab>
                      <span class="tab-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                          <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                        </svg>
                      </span>
                      附加到项目
                    </template>
                    <div class="pane-content">
                      <ChallengeProjectAttach
                        :challenge-id="challengeId"
                        :latest-version-id="challengeData.latestVersionId"
                      />
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
            <NTabs v-model:value="activeSideTab" type="line" size="small" animated class="side-tabs">
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
      :latest-version-tag="challengeData.latestVersionTag"
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

// 顶部标题栏（紧凑单行 48px）
.draft-header {
  height: 48px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 12px;
  background: $bg-primary;
  border-bottom: 1px solid $border-color;

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }

  .header-icon {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $primary-color;
    border-radius: $border-radius;
    color: white;

    svg {
      width: 15px;
      height: 15px;
    }
  }

  .header-content {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .header-title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: $text-primary;
    white-space: nowrap;
  }

  .header-badge {
    padding: 1px 8px;
    background: $warning-color;
    color: white;
    font-size: 11px;
    font-weight: 500;
    border-radius: 3px;
    white-space: nowrap;

    &.create-badge {
      background: $primary-color;
    }

    &.version-badge {
      background: $info-color;
    }

    &.published-badge {
      background: $success-color;
    }
  }
}

// 入库步骤条（压缩为标题栏内胶囊组）
.hdr-steps {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 2px;
  overflow: hidden;
  padding: 0 8px;

  .step {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 12px;
    color: $text-secondary;
    white-space: nowrap;
    padding: 2px 8px 2px 3px;
    border-radius: 999px;
    cursor: pointer;
    transition: all 0.15s ease;

    &:hover {
      background: $bg-hover;
    }

    &.is-done {
      color: #059669;
    }

    &.is-current:not(.is-done),
    &.is-current-done {
      color: $primary-color;
      font-weight: 600;
      background: $primary-light;
    }
  }

  .step-num {
    width: 16px;
    height: 16px;
    min-width: 16px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    font-weight: 600;
    background: $bg-tertiary;
    color: $text-muted;
    border: 1px solid $border-color;
    transition: all 0.15s ease;

    svg {
      width: 9px;
      height: 9px;
    }
  }

  .step.is-done .step-num {
    background: $success-color;
    border-color: $success-color;
    color: white;
  }

  .step.is-current:not(.is-done) .step-num,
  .step.is-current-done .step-num {
    background: $primary-color;
    border-color: $primary-color;
    color: white;
  }

  .step-sep {
    width: 10px;
    height: 1px;
    background: #d1d5db;
    flex-shrink: 0;
  }
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

// 创建模式引导提示
.create-tip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  margin-bottom: 16px;
  background: $primary-light;
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: $border-radius;
  color: $primary-color;
  font-size: 13px;
  line-height: 1.5;

  svg {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
  }
}

// 主内容区域
.draft-main {
  flex: 1;
  padding: 8px;
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
}

// 标签页容器
.tab-nav-wrapper {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

// 紧凑标签页（line 型）
.cyber-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;

  :deep(.n-tabs-nav) {
    background: $bg-primary;
    border: 1px solid $border-color;
    border-bottom: none;
    border-radius: $border-radius $border-radius 0 0;
    padding: 0 6px;
    flex-shrink: 0;
  }

  :deep(.n-tabs-tab) {
    padding: 7px 12px;
    font-size: 12.5px;
    font-weight: 500;
    color: $text-secondary;

    &:hover {
      color: $primary-color;
    }

    &.n-tabs-tab--active {
      color: $primary-color;
      font-weight: 600;
    }
  }

  // 标签内容区域 - 关键：让这个区域可以滚动
  :deep(.n-tabs-content) {
    flex: 1;
    overflow: hidden;
    min-height: 0;
    border: 1px solid $border-color;
    border-radius: 0 0 $border-radius $border-radius;
    background: $bg-primary;
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
    padding: 0;
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
  padding: 10px;
  height: 100%;
  box-sizing: border-box;
}

// 右侧面板（紧凑：边栏式 + 内部滚动）
.side-panel {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: $bg-primary;
  border-left: 1px solid $border-color;
}

.side-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;

  :deep(.n-tabs-nav) {
    padding: 0 8px;
    flex-shrink: 0;
  }

  :deep(.n-tabs-tab) {
    padding: 7px 10px;
    font-size: 12.5px;
    font-weight: 500;
    color: $text-secondary;

    &.n-tabs-tab--active {
      color: $primary-color;
      font-weight: 600;
    }
  }

  :deep(.n-tabs-content) {
    flex: 1;
    overflow: hidden;
    min-height: 0;
  }

  :deep(.n-tabs-pane-wrapper) {
    height: 100%;
    overflow: hidden;
    min-height: 0;
  }

  :deep(.n-tab-pane) {
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    min-height: 0;
    padding: 0;
  }
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
  margin-bottom: 10px;

  &:last-child {
    margin-bottom: 0;
  }

  &.full-height {
    height: calc(100% - 24px);
  }
}

.form-label {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
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
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: $text-primary;
}

.flag-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.flag-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  padding: 5px 10px;
  background: $bg-secondary;
  border-bottom: 1px solid $border-color;
}

.flag-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.flag-index {
  font-size: 12.5px;
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
  padding: 10px 12px;
}

// 空状态
.empty-state {
  padding: 32px 0;
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

import {ref, watch, type Ref} from 'vue';
import type {RouteLocationNormalizedLoaded, Router} from 'vue-router';

type QueryValue = unknown;

function parseQueryString(raw: QueryValue) {
  if (Array.isArray(raw)) return (raw[0] as string) || null;
  if (typeof raw === 'string') return raw || null;
  return null;
}

export function useTabQuerySync(options: {
  route: RouteLocationNormalizedLoaded;
  router: Router;
  draftData: Ref<Api.Cch.ChallengeDraft | null>;
  /** 题目最新发版版本ID（非空时「附加到项目」Tab 才可用） */
  latestVersionId?: Ref<CommonType.IdType | null | undefined>;
  mainKey?: string;
  sideKey?: string;
}) {
  const {route, router, draftData, latestVersionId} = options;
  const mainKey = options.mainKey ?? 'tab';
  const sideKey = options.sideKey ?? 'side';

  const activeMainTab = ref<string>('info');
  const activeSideTab = ref<string>('history');

  function getAvailableMainTabs() {
    const runType = draftData.value?.config?.runType;
    const tabs = ['info', 'flag', 'publish'] as string[];
    if (runType === 'container') {
      tabs.push('container', 'container-target');
    } else if (runType === 'vm') {
      tabs.push('vm');
    }
    // 已发版入库后，「附加到项目」Tab 可用
    if (latestVersionId?.value) {
      tabs.push('attach');
    }
    return tabs;
  }
  function normalizeMainTab(tab: string | null | undefined) {
    const t = (tab || '').trim();
    if (!t) return 'info';
    const available = getAvailableMainTabs();
    if (available.includes(t)) return t;
    return 'info';
  }

  function normalizeSideTab(tab: string | null | undefined) {
    const t = (tab || '').trim();
    return t === 'oasis' ? 'oasis' : 'history';
  }

  // URL -> Tab（支持浏览器前进/后退、手动修改URL）
  watch(
    () => route.query[mainKey],
    (val) => {
      const normalized = normalizeMainTab(parseQueryString(val));
      if (activeMainTab.value !== normalized) activeMainTab.value = normalized;
    },
    {immediate: true}
  );

  watch(
    () => route.query[sideKey],
    (val) => {
      const normalized = normalizeSideTab(parseQueryString(val));
      if (activeSideTab.value !== normalized) activeSideTab.value = normalized;
    },
    {immediate: true}
  );

  // Tab -> URL（选中tab时写回URL，避免刷新丢失）
  watch(
    activeMainTab,
    (val) => {
      const normalized = normalizeMainTab(val);
      if (route.query[mainKey] === normalized) return;
      router.replace({
        path: route.path,
        query: {
          ...route.query,
          [mainKey]: normalized
        }
      });
    },
    {flush: 'post'}
  );

  watch(
    activeSideTab,
    (val) => {
      const normalized = normalizeSideTab(val);
      if (route.query[sideKey] === normalized) return;
      router.replace({
        path: route.path,
        query: {
          ...route.query,
          [sideKey]: normalized
        }
      });
    },
    {flush: 'post'}
  );

  // 数据加载完成后，基于 runType 校验一次主Tab（比如URL里写了container，但实际不是container）
  watch(
    () => draftData.value?.config?.runType,
    () => {
      // 优先使用 URL 中的 tab（例如 container/container-target 只有在 runType 已知后才合法）
      const fromQuery = parseQueryString(route.query[mainKey]);
      const normalized = normalizeMainTab(fromQuery ?? activeMainTab.value);
      if (activeMainTab.value !== normalized) activeMainTab.value = normalized;
    }
  );

  // 发版状态变化时同步校验（例如发版后 attach Tab 变为可用）
  watch(
    () => latestVersionId?.value,
    () => {
      const fromQuery = parseQueryString(route.query[mainKey]);
      const normalized = normalizeMainTab(fromQuery ?? activeMainTab.value);
      if (activeMainTab.value !== normalized) activeMainTab.value = normalized;
    }
  );

  return {
    activeMainTab,
    activeSideTab
  };
}


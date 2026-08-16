<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { fetchDashboardStatistics } from '@/service/api/cch/dashboard';
import { useAppStore } from '@/store/modules/app';
import HeaderBanner from './modules/header-banner.vue';
import CardData from './modules/card-data.vue';
import LineChart from './modules/line-chart.vue';
import PieChart from './modules/pie-chart.vue';
import ProjectNews from './modules/project-news.vue';

const appStore = useAppStore();

const gap = computed(() => (appStore.isMobile ? 0 : 16));
const loading = ref(true);
const dashboard = ref<Api.Cch.DashboardStatistics | null>(null);

async function loadDashboard() {
  loading.value = true;
  try {
    const { data, error } = await fetchDashboardStatistics();
    if (!error) {
      dashboard.value = data;
    }
  } catch {
    dashboard.value = null;
  } finally {
    loading.value = false;
  }
}

onMounted(loadDashboard);
</script>

<template>
  <NSpin :show="loading" class="w-full">
    <NSpace v-if="dashboard" vertical :size="16">
      <HeaderBanner :overview="dashboard.overview" />
      <CardData :overview="dashboard.overview" />
      <NGrid :x-gap="gap" :y-gap="16" responsive="screen" item-responsive>
        <NGi span="24 s:24 m:14">
          <NCard title="近6个月创建趋势" :bordered="false" class="card-wrapper">
            <LineChart :trend="dashboard.trend" />
          </NCard>
        </NGi>
        <NGi span="24 s:24 m:10">
          <NCard title="题目类型分布" :bordered="false" class="card-wrapper">
            <PieChart :distribution="dashboard.categoryDistribution" />
          </NCard>
        </NGi>
      </NGrid>
      <NGrid :x-gap="gap" :y-gap="16" responsive="screen" item-responsive>
        <NGi span="24 s:24 m:14">
          <ProjectNews :projects="dashboard.recentProjects" />
        </NGi>
        <NGi span="24 s:24 m:10">
          <NCard title="项目类型分布" :bordered="false" size="small" class="card-wrapper">
            <PieChart :distribution="dashboard.projectTypeDistribution" :is-doughnut="false" class="h-full" />
          </NCard>
        </NGi>
      </NGrid>
    </NSpace>
    <NEmpty v-else-if="!loading" description="暂无统计数据或加载失败" />
  </NSpin>
</template>

<style scoped></style>

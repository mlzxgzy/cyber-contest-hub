<script setup lang="ts">
import { watch } from 'vue';
import { useAppStore } from '@/store/modules/app';
import { useEcharts } from '@/hooks/common/echarts';
import { $t } from '@/locales';

defineOptions({
  name: 'LineChart'
});

const props = defineProps<{
  trend: Api.Cch.DashboardTrendItem[];
}>();

const appStore = useAppStore();

const { domRef, updateOptions } = useEcharts(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross',
      label: {
        backgroundColor: '#6a7985'
      }
    }
  },
  legend: {
    data: [$t('page.home.challengeCount'), $t('page.home.versionCount'), $t('page.home.projectCount')],
    top: '0'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '15%'
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: [] as string[]
  },
  yAxis: {
    type: 'value',
    minInterval: 1
  },
  series: [
    {
      color: '#8e9dff',
      name: $t('page.home.challengeCount'),
      type: 'line',
      smooth: true,
      stack: 'Total',
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0.25,
              color: '#8e9dff'
            },
            {
              offset: 1,
              color: '#fff'
            }
          ]
        }
      },
      emphasis: {
        focus: 'series'
      },
      data: [] as number[]
    },
    {
      color: '#26deca',
      name: $t('page.home.versionCount'),
      type: 'line',
      smooth: true,
      stack: 'Total',
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0.25,
              color: '#26deca'
            },
            {
              offset: 1,
              color: '#fff'
            }
          ]
        }
      },
      emphasis: {
        focus: 'series'
      },
      data: [] as number[]
    },
    {
      color: '#fedc69',
      name: $t('page.home.projectCount'),
      type: 'line',
      smooth: true,
      stack: 'Total',
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0.25,
              color: '#fedc69'
            },
            {
              offset: 1,
              color: '#fff'
            }
          ]
        }
      },
      emphasis: {
        focus: 'series'
      },
      data: [] as number[]
    }
  ]
}));

function renderTrend() {
  updateOptions(opts => {
    opts.xAxis.data = props.trend.map(item => item.month);
    opts.series[0].data = props.trend.map(item => item.challengeCount);
    opts.series[1].data = props.trend.map(item => item.versionCount);
    opts.series[2].data = props.trend.map(item => item.projectCount);

    return opts;
  });
}

function updateLocale() {
  updateOptions((opts, factory) => {
    const originOpts = factory();

    opts.legend.data = originOpts.legend.data;
    opts.series[0].name = originOpts.series[0].name;
    opts.series[1].name = originOpts.series[1].name;
    opts.series[2].name = originOpts.series[2].name;

    return opts;
  });
}

watch(
  () => props.trend,
  () => {
    renderTrend();
  },
  { immediate: true, deep: true }
);

watch(
  () => appStore.locale,
  () => {
    updateLocale();
  }
);
</script>

<template>
  <div ref="domRef" class="h-360px overflow-hidden"></div>
</template>

<style scoped></style>

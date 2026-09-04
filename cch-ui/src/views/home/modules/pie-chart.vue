<script setup lang="ts">
import { computed, watch } from 'vue';
import { useEcharts } from '@/hooks/common/echarts';
import { useDict } from '@/hooks/business/dict';

defineOptions({
  name: 'PieChart'
});

const props = withDefaults(
  defineProps<{
    distribution: Api.Cch.DashboardNameValue[];
    isDoughnut?: boolean;
  }>(),
  {
    isDoughnut: true
  }
);

const { record } = useDict('cch_question_categroy');

const projectTypeLabelMap: Record<string, string> = {
  normal: '普通项目',
  contest: '竞赛项目',
  authoring: '出题项目'
};

const chartData = computed(() =>
  props.distribution.map(item => ({
    name: record.value[item.name] || projectTypeLabelMap[item.name] || item.name,
    value: item.value
  }))
);

const { domRef, updateOptions } = useEcharts(() => ({
  tooltip: {
    trigger: 'item'
  },
  legend: {
    bottom: '1%',
    left: 'center',
    itemStyle: {
      borderWidth: 0
    }
  },
  series: [
    {
      color: ['#5da8ff', '#8e9dff', '#fedc69', '#26deca', '#ec4786', '#56cdf3'],
      name: '数据分布',
      type: 'pie',
      radius: props.isDoughnut ? ['45%', '75%'] : '55%',
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 1
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '12'
        }
      },
      labelLine: {
        show: false
      },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

function renderChart() {
  updateOptions(opts => {
    opts.series[0].data = chartData.value;
    opts.series[0].radius = props.isDoughnut ? ['45%', '75%'] : '55%';
    opts.series[0].label.show = false;

    return opts;
  });
}

watch(
  () => [props.distribution, record.value],
  () => {
    renderChart();
  },
  { immediate: true, deep: true }
);
</script>

<template>
  <div ref="domRef" class="h-360px overflow-hidden"></div>
</template>

<style scoped></style>

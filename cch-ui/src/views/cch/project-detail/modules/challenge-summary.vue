<script lang="ts" setup>
import {computed, h, onMounted, ref, watch} from 'vue';
import type {DataTableColumns} from 'naive-ui';
import {fetchGetProjectChallenges} from '@/service/api/cch/project';
import {useDict} from '@/hooks/business/dict';
import {useEcharts} from '@/hooks/common/echarts';

defineOptions({
  name: 'ChallengeSummary'
});

interface Props {
  projectId: CommonType.IdType;
}

const props = defineProps<Props>();

/** 汇总分类维度：题目类型 / 标签 / 导入人 */
type SummaryDimension = 'category' | 'tag' | 'creator';

const dimension = ref<SummaryDimension>('category');

const dimensionOptions: Array<{label: string; value: SummaryDimension}> = [
  {label: '按题目类型', value: 'category'},
  {label: '按标签', value: 'tag'},
  {label: '按导入人', value: 'creator'}
];

// 题目类型字典（value -> label）
const {record: categoryRecord} = useDict('cch_question_categroy');

const loading = ref(false);
const challenges = ref<Api.Cch.ProjectChallenge[]>([]);

/** 拆分逗号分隔的标签字符串 */
function parseTags(tags?: string | null): string[] {
  if (!tags) return [];
  return tags
    .split(',')
    .map(item => item.trim())
    .filter(Boolean);
}

/** 题目类型显示名（字典翻译，未设置时归为"未分类"） */
function categoryLabel(value?: string | null): string {
  if (!value) return '未分类';
  return categoryRecord.value[value] || value;
}

interface SummaryRow {
  /** 分类名 */
  name: string;
  /** 数量（道） */
  count: number;
  /** 占比 */
  percent: string;
  /** 该分类下的题目名称明细 */
  challenges: string[];
}

/** 按当前维度分类汇总（类似 Excel 分类汇总） */
const summaryRows = computed<SummaryRow[]>(() => {
  const total = challenges.value.length;
  const map = new Map<string, SummaryRow>();

  const pushItem = (key: string, challengeName: string) => {
    let row = map.get(key);
    if (!row) {
      row = {name: key, count: 0, percent: '0%', challenges: []};
      map.set(key, row);
    }
    row.count += 1;
    row.challenges.push(challengeName);
  };

  challenges.value.forEach(row => {
    const name = row.challengeName || `题目#${row.challengeId}`;
    if (dimension.value === 'category') {
      pushItem(categoryLabel(row.category), name);
    } else if (dimension.value === 'tag') {
      // 一题多标签时，每个标签各计一次
      const tags = parseTags(row.tags);
      if (tags.length) {
        tags.forEach(tag => pushItem(tag, name));
      } else {
        pushItem('未打标签', name);
      }
    } else {
      pushItem(row.createByName || '未知导入人', name);
    }
  });

  return Array.from(map.values())
    .map(row => ({
      ...row,
      percent: total ? `${((row.count / total) * 100).toFixed(1)}%` : '0%'
    }))
    .sort((a, b) => b.count - a.count || a.name.localeCompare(b.name, 'zh-CN'));
});

// ===== 汇总统计 =====
const totalCount = computed(() => challenges.value.length);
const categoryKindCount = computed(
  () => new Set(challenges.value.map(row => row.category || '')).size
);
const tagKindCount = computed(() => {
  const set = new Set<string>();
  challenges.value.forEach(row => parseTags(row.tags).forEach(tag => set.add(tag)));
  return set.size;
});

// ===== 柱状图 =====
const {domRef, updateOptions} = useEcharts(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {type: 'shadow'}
  },
  grid: {left: 8, right: 16, top: 32, bottom: 8, containLabel: true},
  xAxis: {
    type: 'category',
    data: [] as string[],
    axisLabel: {
      interval: 0,
      rotate: 30,
      formatter: (value: string) => (value.length > 8 ? `${value.slice(0, 8)}…` : value)
    }
  },
  yAxis: {
    type: 'value',
    name: '题目数量',
    minInterval: 1
  },
  series: [
    {
      name: '题目数量',
      type: 'bar',
      barMaxWidth: 40,
      itemStyle: {borderRadius: [4, 4, 0, 0]},
      label: {show: true, position: 'top'},
      data: [] as number[]
    }
  ]
}));

function renderChart() {
  updateOptions(opts => {
    opts.xAxis.data = summaryRows.value.map(row => row.name);
    opts.series[0].data = summaryRows.value.map(row => row.count);
    return opts;
  });
}

watch(summaryRows, renderChart, {immediate: true, deep: true});

// ===== 汇总明细表格 =====
const summaryColumns: DataTableColumns<SummaryRow> = [
  {key: 'index', title: '#', width: 48, align: 'center', render: (_row, index) => index + 1},
  {key: 'name', title: '分类', minWidth: 120},
  {key: 'count', title: '数量（道）', width: 110, align: 'center'},
  {key: 'percent', title: '占比', width: 100, align: 'center'},
  {
    key: 'challenges',
    title: '题目明细',
    minWidth: 220,
    render: row => h('span', {class: 'whitespace-pre-wrap text-xs'}, row.challenges.join('、'))
  }
];

async function loadChallenges() {
  loading.value = true;
  const {data, error} = await fetchGetProjectChallenges(props.projectId);
  if (error) {
    window.$message?.error(error.message || '加载赛题汇总失败');
  } else {
    challenges.value = data || [];
  }
  loading.value = false;
}

onMounted(() => {
  loadChallenges();
});
</script>

<template>
  <div class="flex flex-col gap-8px">
    <NDivider title-placement="left">赛题汇总</NDivider>

    <div class="flex flex-wrap items-center justify-between gap-8px">
      <div class="flex flex-wrap gap-24px text-sm">
        <span>赛题总数：<b class="text-primary">{{ totalCount }}</b> 道</span>
        <span>题目类型：<b class="text-primary">{{ categoryKindCount }}</b> 种</span>
        <span>标签种类：<b class="text-primary">{{ tagKindCount }}</b> 个</span>
      </div>
      <div class="flex items-center gap-8px">
        <NRadioGroup v-model:value="dimension" size="small">
          <NRadioButton
            v-for="item in dimensionOptions"
            :key="item.value"
            :value="item.value"
            :label="item.label"
          />
        </NRadioGroup>
        <NButton size="small" quaternary @click="loadChallenges">
          <template #icon>
            <icon-ic-round-refresh class="text-icon"/>
          </template>
          刷新
        </NButton>
      </div>
    </div>

    <NEmpty v-if="!loading && !challenges.length" description="本项目暂无赛题" size="small" />
    <template v-else>
      <div ref="domRef" class="h-300px w-full overflow-hidden" />
      <NDataTable
        size="small"
        :columns="summaryColumns"
        :data="summaryRows"
        :loading="loading"
        :max-height="320"
      />
    </template>
  </div>
</template>

<style scoped></style>

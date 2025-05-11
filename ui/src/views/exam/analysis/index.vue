<template>
  <div class="app-container">
    <!-- 数据概览卡片 -->
    <el-row :gutter="20" class="mb-8">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">
            <div class="card-icon bg-blue">
              <i class="el-icon-trophy"></i>
            </div>
            <div class="card-info">
              <div class="card-title">总竞赛数</div>
              <div class="card-value">{{ statsData.competitionCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">
            <div class="card-icon bg-green">
              <i class="el-icon-user"></i>
            </div>
            <div class="card-info">
              <div class="card-title">参赛选手数</div>
              <div class="card-value">{{ statsData.competitorCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">
            <div class="card-icon bg-purple">
              <i class="el-icon-collection-tag"></i>
            </div>
            <div class="card-info">
              <div class="card-title">总题目数</div>
              <div class="card-value">{{ statsData.questionCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="card-content">
            <div class="card-icon bg-orange">
              <i class="el-icon-data-analysis"></i>
            </div>
            <div class="card-info">
              <div class="card-title">平均难度</div>
              <div class="card-value">{{ statsData.avgDifficulty }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表展示 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-title">题目类型分布</div>
          </template>
          <div ref="typeChart" style="height: 400px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-title">竞赛时间分布</div>
          </template>
          <div ref="timeChart" style="height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-8">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-title">题目难度分布</div>
          </template>
          <div ref="difficultyChart" style="height: 400px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="chart-title">活跃选手TOP10</div>
          </template>
          <el-table :data="activeCompetitors" stripe style="width: 100%">
            <el-table-column prop="rank" label="排名" width="80"></el-table-column>
            <el-table-column prop="name" label="姓名"></el-table-column>
            <el-table-column prop="count" label="参赛次数"></el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button link type="primary" @click="viewCompetitor(scope.row)"
                >查看详情
                </el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog title="选手信息" v-model="competitorDetailDialog.open" width="80%" append-to-body>
      <competor-detail :id="competitorDetailDialog.id"/>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import * as echarts from 'echarts'
import {getAnalysisData} from '@/api/exam/analysis'
import CompetorDetail from "@/views/exam/analysis/competorDetail.vue";

const competitorDetailDialog = ref({
  open: false,
  id: null,
})
const statsData = ref({
  competitionCount: 0,
  competitorCount: 0,
  questionCount: 0,
  avgDifficulty: 0
})
const typeDistribution = ref(null)
const timeDistribution = ref(null)
const difficultyDistribution = ref(null)

const activeCompetitors = ref([])
const typeChart = ref(null)
const timeChart = ref(null)
const difficultyChart = ref(null)

// 初始化图表
const initCharts = () => {
  // 题目类型饼图
  const typeInstance = echarts.init(typeChart.value)
  typeInstance.setOption({
    tooltip: {trigger: 'item'},
    legend: {bottom: 10, left: 'center'},
    series: [{
      name: '类型分布',
      type: 'pie',
      radius: ['40%', '70%'],
      roseType: 'radius',
      itemStyle: {borderRadius: 8},
      data: typeDistribution.value
    }]
  })

  // 竞赛时间折线图
  const timeInstance = echarts.init(timeChart.value)
  timeInstance.setOption({
    tooltip: {trigger: 'axis'},
    xAxis: {type: 'category', data: timeDistribution.value.map(d => d.date)},
    yAxis: {type: 'value'},
    series: [{
      data: timeDistribution.value.map(d => d.count),
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {offset: 0, color: 'rgba(64, 158, 255, 0.8)'},
          {offset: 1, color: 'rgba(64, 158, 255, 0.2)'}
        ])
      }
    }]
  })

  // 题目难度柱状图
  const difficultyInstance = echarts.init(difficultyChart.value)
  difficultyInstance.setOption({
    tooltip: {trigger: 'axis'},
    xAxis: {type: 'category', data: difficultyDistribution.value.map(d => d.level)},
    yAxis: {type: 'value'},
    series: [{
      data: difficultyDistribution.value.map(d => d.count),
      type: 'bar',
      barWidth: '60%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          {offset: 0, color: '#83bff6'},
          {offset: 0.5, color: '#188df0'},
          {offset: 1, color: '#188df0'}
        ])
      }
    }]
  })
}

// 获取数据
const fetchData = async () => {
  const res = await getAnalysisData()
  statsData.value = res.data.stats
  activeCompetitors.value = res.data.activeCompetitors
  typeDistribution.value = res.data.typeDistribution
  timeDistribution.value = res.data.timeDistribution
  difficultyDistribution.value = res.data.difficultyDistribution
  initCharts()
}

// 查看选手详情
const viewCompetitor = (row) => {
  competitorDetailDialog.value.id = row.id
  competitorDetailDialog.value.open = true
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 20px;
}

.card-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.card-info {
  margin-left: 20px;
}

.card-title {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
}

.bg-blue {
  background: #409EFF;
}

.bg-green {
  background: #67C23A;
}

.bg-purple {
  background: #8A2BE2;
}

.bg-orange {
  background: #E6A23C;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.mt-8 {
  margin-top: 20px;
}
</style>

<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>选手基本信息</span>
          <el-button type="primary" @click="emits('close')">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ competitorInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ competitorInfo.mobile || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ competitorInfo.idcard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="参赛次数">{{ competitionList.length }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ competitorInfo.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="box-card mt-8">
      <template #header>
        <div class="card-header">
          <span>参与竞赛记录</span>
        </div>
      </template>

      <el-table :data="competitionList" stripe>
        <el-table-column prop="name" label="竞赛名称" width="200"></el-table-column>
        <el-table-column prop="date" label="竞赛日期" width="180">
          <template #default="{row}">
            {{ parseTime(row.date, '{y}-{m}-{d}') }}
          </template>
        </el-table-column>
        <el-table-column prop="focus" label="竞赛方向"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import {getCompetitorDetail} from '@/api/exam/competitor.js'

const props = defineProps({
  id: {
    type: Number,
    required: true,
    default: "选手ID"
  },
});

const emits = defineEmits(['close'])

const competitorId = ref(null)
const competitorInfo = ref({})
const competitionList = ref([])

const fetchData = async () => {
  try {
    const res = await getCompetitorDetail(props.id)
    competitorInfo.value = res.data.competitor
    competitionList.value = res.data.competitions
  } catch {
    // 错误处理
  }
}

const viewCompetition = (id) => {
  // 跳转到竞赛详情页，需根据实际路由配置
  console.log('查看竞赛:', id)
}

onMounted(() => {
  competitorId.value = props.id
  fetchData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mt-8 {
  margin-top: 20px;
}
</style>

import request from '@/utils/request'

// 查询数据分析
export function getAnalysisData(params) {
  return request({
    url: '/exam/analysis/data',
    method: 'get',
    params
  })
}
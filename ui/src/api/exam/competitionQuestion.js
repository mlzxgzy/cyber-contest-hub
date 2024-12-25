import request from '@/utils/request'

// 查询竞赛题目列表
export function listCompetitionQuestion(query) {
  return request({
    url: '/exam/competitionQuestion/list',
    method: 'get',
    params: query
  })
}

// 查询竞赛题目详细
export function getCompetitionQuestion(id) {
  return request({
    url: '/exam/competitionQuestion/' + id,
    method: 'get'
  })
}

// 新增竞赛题目
export function addCompetitionQuestion(data) {
  return request({
    url: '/exam/competitionQuestion',
    method: 'post',
    data: data
  })
}

// 修改竞赛题目
export function updateCompetitionQuestion(data) {
  return request({
    url: '/exam/competitionQuestion',
    method: 'put',
    data: data
  })
}

// 删除竞赛题目
export function delCompetitionQuestion(id) {
  return request({
    url: '/exam/competitionQuestion/' + id,
    method: 'delete'
  })
}

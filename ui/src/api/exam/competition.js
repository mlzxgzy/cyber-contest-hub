import request from '@/utils/request'

// 查询竞赛列表
export function listCompetition(query) {
  return request({
    url: '/exam/competition/list',
    method: 'get',
    params: query
  })
}

// 查询竞赛详细
export function getCompetition(id) {
  return request({
    url: '/exam/competition/' + id,
    method: 'get'
  })
}

// 新增竞赛
export function addCompetition(data) {
  return request({
    url: '/exam/competition',
    method: 'post',
    data: data
  })
}

// 修改竞赛
export function updateCompetition(data) {
  return request({
    url: '/exam/competition',
    method: 'put',
    data: data
  })
}

// 删除竞赛
export function delCompetition(id) {
  return request({
    url: '/exam/competition/' + id,
    method: 'delete'
  })
}

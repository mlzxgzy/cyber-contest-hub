import request from '@/utils/request'

// 查询选手列表
export function listCompetitor(query) {
  return request({
    url: '/exam/competitor/list',
    method: 'get',
    params: query
  })
}

// 查询选手详细
export function getCompetitor(id) {
  return request({
    url: '/exam/competitor/' + id,
    method: 'get'
  })
}

// 新增选手
export function addCompetitor(data) {
  return request({
    url: '/exam/competitor',
    method: 'post',
    data: data
  })
}

// 修改选手
export function updateCompetitor(data) {
  return request({
    url: '/exam/competitor',
    method: 'put',
    data: data
  })
}

// 删除选手
export function delCompetitor(id) {
  return request({
    url: '/exam/competitor/' + id,
    method: 'delete'
  })
}

import request from '@/utils/request'

// 查询竞赛选手列表
export function listCompetiitonCompetitor(query) {
  return request({
    url: '/exam/competiitonCompetitor/list',
    method: 'get',
    params: query
  })
}

// 查询竞赛选手详细
export function getCompetiitonCompetitor(id) {
  return request({
    url: '/exam/competiitonCompetitor/' + id,
    method: 'get'
  })
}

// 新增竞赛选手
export function addCompetiitonCompetitor(data) {
  return request({
    url: '/exam/competiitonCompetitor',
    method: 'post',
    data: data
  })
}

// 修改竞赛选手
export function updateCompetiitonCompetitor(data) {
  return request({
    url: '/exam/competiitonCompetitor',
    method: 'put',
    data: data
  })
}

// 删除竞赛选手
export function delCompetiitonCompetitor(id) {
  return request({
    url: '/exam/competiitonCompetitor/' + id,
    method: 'delete'
  })
}

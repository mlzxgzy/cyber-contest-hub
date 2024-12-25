import request from '@/utils/request'

// 查询题目Tag列表
export function listQuestionTag(query) {
  return request({
    url: '/exam/questionTag/list',
    method: 'get',
    params: query
  })
}

// 查询题目Tag详细
export function getQuestionTag(id) {
  return request({
    url: '/exam/questionTag/' + id,
    method: 'get'
  })
}

// 新增题目Tag
export function addQuestionTag(data) {
  return request({
    url: '/exam/questionTag',
    method: 'post',
    data: data
  })
}

// 修改题目Tag
export function updateQuestionTag(data) {
  return request({
    url: '/exam/questionTag',
    method: 'put',
    data: data
  })
}

// 删除题目Tag
export function delQuestionTag(id) {
  return request({
    url: '/exam/questionTag/' + id,
    method: 'delete'
  })
}

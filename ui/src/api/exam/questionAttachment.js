import request from '@/utils/request'

// 查询题目附件列表
export function listQuestionAttachment(query) {
  return request({
    url: '/exam/questionAttachment/list',
    method: 'get',
    params: query
  })
}

// 查询题目附件详细
export function getQuestionAttachment(id) {
  return request({
    url: '/exam/questionAttachment/' + id,
    method: 'get'
  })
}

// 新增题目附件
export function addQuestionAttachment(data) {
  return request({
    url: '/exam/questionAttachment',
    method: 'post',
    data: data
  })
}

// 修改题目附件
export function updateQuestionAttachment(data) {
  return request({
    url: '/exam/questionAttachment',
    method: 'put',
    data: data
  })
}

// 删除题目附件
export function delQuestionAttachment(id) {
  return request({
    url: '/exam/questionAttachment/' + id,
    method: 'delete'
  })
}

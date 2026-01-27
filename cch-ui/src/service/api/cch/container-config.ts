import { request } from '@/service/request';

/** 获取容器配置列表 */
export function fetchGetContainerConfigList(params?: Api.Cch.ContainerConfigSearchParams) {
  return request<Api.Cch.ContainerConfigList>({
    url: '/cch/container/config/list',
    method: 'get',
    params
  });
}

/** 新增容器配置 */
export function fetchCreateContainerConfig(data: Api.Cch.ContainerConfigOperateParams) {
  return request<boolean>({
    url: '/cch/container/config',
    method: 'post',
    data
  });
}

/** 修改容器配置 */
export function fetchUpdateContainerConfig(data: Api.Cch.ContainerConfigOperateParams) {
  return request<boolean>({
    url: '/cch/container/config',
    method: 'put',
    data
  });
}

/** 批量删除容器配置 */
export function fetchBatchDeleteContainerConfig(ids: CommonType.IdType[]) {
  return request<boolean>({
    url: `/cch/container/config/${ids.join(',')}`,
    method: 'delete'
  });
}

/** 测试连接 */
export function fetchTestConnection(id: CommonType.IdType) {
  return request<boolean>({
    url: `/cch/container/config/test/${id}`,
    method: 'post'
  });
}

/** 获取当前活跃的容器实例 */
export function fetchGetActiveInstance() {
  return request<Api.Cch.ContainerConfig | null>({
    url: '/cch/container/config/active',
    method: 'get'
  });
}

/** 断开当前活跃的容器实例 */
export function fetchDisconnect() {
  return request<boolean>({
    url: '/cch/container/config/disconnect',
    method: 'post'
  });
}

/** 获取Docker容器列表 */
export function fetchGetContainerList() {
  return request<Api.Cch.DockerContainer[]>({
    url: '/cch/container/config/containers',
    method: 'get'
  });
}

/** 获取Docker镜像列表 */
export function fetchGetImageList() {
  return request<Api.Cch.DockerImage[]>({
    url: '/cch/container/config/images',
    method: 'get'
  });
}


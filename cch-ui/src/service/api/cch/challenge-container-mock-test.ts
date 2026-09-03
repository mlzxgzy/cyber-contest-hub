import {request} from '@/service/request';

/**
 * 容器模拟测试 - 来源选项（用于前端下拉选择）
 */
export namespace ContainerMockTest {
  /** 来源选项 */
  export interface SourceOption {
    id: number; // 草稿ID或版本ID
    draftId: number; // 最终使用的草稿ID
    name: string; // 显示名称
    sourceType: 'draft' | 'version';
    challengeName: string;
    versionTag?: string;
    /** 草稿版本号（仅草稿来源有值） */
    draftVersion?: number;
    createTime?: string; // 创建时间（修改时间）
  }

  /** 容器暴露信息 */
  export interface ContainerInfo {
    name: string;
    host: string;
    protocol: string;
    internalPort: number;
    externalPort: number;
  }

  /** 测试详情 */
  export interface TestDetail {
    id: number;
    draftId: number;
    sourceType: 'draft' | 'version';
    sourceId: number;
    challengeName: string;
    containerIds: string[];
    containers: ContainerInfo[];
    status: 'starting' | 'running' | 'failed' | 'destroying' | 'expired';
    errorMsg?: string | null;
    createTime: string;
    expireTime: string;
    remainingSeconds: number;
    extendCount: number;
  }
}

/** 获取可选来源列表（同一题目下的草稿+版本） */
export function fetchContainerMockTestSources(challengeId: string) {
  return request<ContainerMockTest.SourceOption[]>({
    url: '/cch/containerMockTest/sources',
    method: 'get',
    params: {challengeId: challengeId}
  });
}

/** 启动容器模拟测试 */
export function fetchStartContainerMockTest(params: { sourceType: string; sourceId: number }) {
  return request<ContainerMockTest.TestDetail>({
    url: '/cch/containerMockTest/start',
    method: 'post',
    data: params
  });
}

/** 获取测试详情 */
export function fetchContainerMockTestDetail(id: number) {
  return request<ContainerMockTest.TestDetail>({
    url: `/cch/containerMockTest/${id}`,
    method: 'get'
  });
}

/** 获取我的活跃测试列表 */
export function fetchContainerMockTestList() {
  return request<ContainerMockTest.TestDetail[]>({
    url: '/cch/containerMockTest/my-list',
    method: 'get'
  });
}

/** 延长测试时间 */
export function fetchExtendContainerMockTest(id: number, minutes: number = 30) {
  return request<void>({
    url: `/cch/containerMockTest/extend/${id}`,
    method: 'post',
    data: {minutes}
  });
}

/** 销毁测试环境 */
export function fetchDestroyContainerMockTest(id: number) {
  return request<void>({
    url: `/cch/containerMockTest/destroy/${id}`,
    method: 'post'
  });
}

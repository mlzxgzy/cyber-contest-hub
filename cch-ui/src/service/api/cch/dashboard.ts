import { request } from '@/service/request';

/**
 * 获取首页仪表盘统计数据
 */
export function fetchDashboardStatistics() {
  return request<Api.Cch.DashboardStatistics>({
    url: '/cch/dashboard/statistics',
    method: 'get'
  });
}

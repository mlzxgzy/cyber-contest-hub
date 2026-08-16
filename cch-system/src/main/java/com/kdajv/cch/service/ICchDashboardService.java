package com.kdajv.cch.service;

import com.kdajv.cch.domain.vo.CchDashboardVo;

/**
 * 首页仪表盘Service接口
 *
 * @author system
 * @date 2026-03-21
 */
public interface ICchDashboardService {

    /**
     * 获取仪表盘统计数据
     *
     * @return 统计数据
     */
    CchDashboardVo getDashboardStatistics();
}

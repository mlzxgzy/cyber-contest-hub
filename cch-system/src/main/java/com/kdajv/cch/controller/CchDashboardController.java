package com.kdajv.cch.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.kdajv.cch.domain.vo.CchDashboardVo;
import com.kdajv.cch.service.ICchDashboardService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页仪表盘Controller
 *
 * @author system
 * @date 2026-03-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/cch/dashboard")
public class CchDashboardController extends BaseController {

    private final ICchDashboardService dashboardService;

    /**
     * 获取首页统计数据
     */
    @SaCheckLogin
    @GetMapping("/statistics")
    public R<CchDashboardVo> statistics() {
        return R.ok(dashboardService.getDashboardStatistics());
    }
}

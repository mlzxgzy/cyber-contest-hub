package com.kdajv.cch.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.kdajv.cch.domain.vo.ChallengeContainerMockTestVo;
import com.kdajv.cch.domain.vo.ContainerMockTestSourceVo;
import com.kdajv.cch.service.IChallengeContainerMockTestService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 容器模拟测试Controller
 *
 * @author system
 * @date 2026-01-30
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/cch/containerMockTest")
public class ChallengeContainerMockTestController extends BaseController {

    private final IChallengeContainerMockTestService containerMockTestService;

    /**
     * 获取可选来源列表（同一题目下的草稿+版本）
     *
     * @param challengeId 题目ID
     * @return 来源选项列表
     */
    @SaCheckPermission("cch:containerMockTest:list")
    @GetMapping("/sources")
    public R<List<ContainerMockTestSourceVo>> getAvailableSources(@RequestParam Long challengeId) {
        return R.ok(containerMockTestService.getAvailableSources(challengeId));
    }

    /**
     * 启动容器模拟测试（异步）
     * <p>
     * 校验并创建 starting 状态的测试记录后立即返回，容器在后台异步启动，
     * 前端通过轮询 /my-list 或 /{id} 获取最终状态（running/failed）。
     *
     * @param params 参数 {sourceType, sourceId}
     * @return 测试详情（status=starting）
     */
    @SaCheckPermission("cch:containerMockTest:add")
    @PostMapping("/start")
    public R<ChallengeContainerMockTestVo> startContainerMockTest(@RequestBody Map<String, Object> params) {
        String sourceType = (String) params.get("sourceType");

        if (sourceType == null) {
            return R.fail("参数不完整");
        }

        Long sourceId = Long.parseLong((String) params.get("sourceId"));
        ChallengeContainerMockTestVo result = containerMockTestService.startContainerMockTest(sourceType, sourceId);
        return R.ok(result);
    }

    /**
     * 获取测试详情
     *
     * @param id 测试ID
     * @return 测试详情
     */
    @SaCheckPermission("cch:containerMockTest:query")
    @GetMapping("/{id}")
    public R<ChallengeContainerMockTestVo> getDetail(@PathVariable Long id) {
        ChallengeContainerMockTestVo result = containerMockTestService.getContainerMockTestDetail(id);
        return R.ok(result);
    }

    /**
     * 获取我的活跃测试列表
     *
     * @return 活跃测试列表
     */
    @SaCheckPermission("cch:containerMockTest:list")
    @GetMapping("/my-list")
    public R<List<ChallengeContainerMockTestVo>> getMyActiveTests() {
        return R.ok(containerMockTestService.getMyActiveTests());
    }

    /**
     * 延长测试时间
     *
     * @param id     测试ID
     * @param params 参数 {minutes}
     * @return 是否成功
     */
    @SaCheckPermission("cch:containerMockTest:edit")
    @PostMapping("/extend/{id}")
    public R<Void> extendTime(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Number minutesNum = (Number) params.get("minutes");
        Integer minutes = minutesNum != null ? minutesNum.intValue() : 30;

        boolean success = containerMockTestService.extendTime(id, minutes);
        return success ? R.ok() : R.fail("延长失败");
    }

    /**
     * 销毁测试环境
     *
     * @param id 测试ID
     * @return 是否成功
     */
    @SaCheckPermission("cch:containerMockTest:remove")
    @PostMapping("/destroy/{id}")
    public R<Void> destroyEnvironment(@PathVariable Long id) {
        boolean success = containerMockTestService.destroyEnvironment(id);
        return success ? R.ok() : R.fail("销毁失败");
    }
}

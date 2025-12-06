package com.kdajv.cch.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.kdajv.cch.domain.bo.ChallengeBo;
import com.kdajv.cch.domain.vo.ChallengeVo;
import com.kdajv.cch.service.IChallengeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目列表
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/cch/challenge")
public class ChallengeController extends BaseController {

    private final IChallengeService challengeService;

    /**
     * 查询题目列表列表
     */
    @SaCheckPermission("cch:challenge:list")
    @GetMapping("/list")
    public TableDataInfo<ChallengeVo> list(ChallengeBo bo, PageQuery pageQuery) {
        return challengeService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出题目列表列表
     */
    @SaCheckPermission("cch:challenge:export")
    @Log(title = "题目列表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ChallengeBo bo, HttpServletResponse response) {
        List<ChallengeVo> list = challengeService.queryList(bo);
        ExcelUtil.exportExcel(list, "题目列表", ChallengeVo.class, response);
    }

    /**
     * 获取题目列表详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("cch:challenge:query")
    @GetMapping("/{id}")
    public R<ChallengeVo> getInfo(@NotNull(message = "主键不能为空")
                                  @PathVariable Long id) {
        return R.ok(challengeService.queryById(id));
    }

    /**
     * 新增题目列表
     */
    @SaCheckPermission("cch:challenge:add")
    @Log(title = "题目列表", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ChallengeBo bo) {
        return toAjax(challengeService.insertByBo(bo));
    }

    /**
     * 修改题目列表
     */
    @SaCheckPermission("cch:challenge:edit")
    @Log(title = "题目列表", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChallengeBo bo) {
        return toAjax(challengeService.updateByBo(bo));
    }

    /**
     * 删除题目列表
     *
     * @param ids 主键串
     */
    @SaCheckPermission("cch:challenge:remove")
    @Log(title = "题目列表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(challengeService.deleteWithValidByIds(List.of(ids), true));
    }
}

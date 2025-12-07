package com.kdajv.cch.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.kdajv.cch.domain.bo.ChallengeVersionBo;
import com.kdajv.cch.domain.vo.ChallengeVersionVo;
import com.kdajv.cch.service.IChallengeVersionService;
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
 * 题目版本
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/cch/challengeVersion")
public class ChallengeVersionController extends BaseController {

    private final IChallengeVersionService challengeVersionService;

    /**
     * 查询题目版本列表
     */
    @SaCheckPermission("cch:challengeVersion:list")
    @GetMapping("/list")
    public TableDataInfo<ChallengeVersionVo> list(ChallengeVersionBo bo, PageQuery pageQuery) {
        return challengeVersionService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出题目版本列表
     */
    @SaCheckPermission("cch:challengeVersion:export")
    @Log(title = "题目版本", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ChallengeVersionBo bo, HttpServletResponse response) {
        List<ChallengeVersionVo> list = challengeVersionService.queryList(bo);
        ExcelUtil.exportExcel(list, "题目版本", ChallengeVersionVo.class, response);
    }

    /**
     * 获取题目版本详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("cch:challengeVersion:query")
    @GetMapping("/{id}")
    public R<ChallengeVersionVo> getInfo(@NotNull(message = "主键不能为空")
                                         @PathVariable Long id) {
        return R.ok(challengeVersionService.queryById(id));
    }

    /**
     * 新增题目版本
     */
    @SaCheckPermission("cch:challengeVersion:add")
    @Log(title = "题目版本", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ChallengeVersionBo bo) {
        return toAjax(challengeVersionService.insertByBo(bo));
    }

    /**
     * 修改题目版本
     */
    @SaCheckPermission("cch:challengeVersion:edit")
    @Log(title = "题目版本", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChallengeVersionBo bo) {
        return toAjax(challengeVersionService.updateByBo(bo));
    }

    /**
     * 删除题目版本
     *
     * @param ids 主键串
     */
    @SaCheckPermission("cch:challengeVersion:remove")
    @Log(title = "题目版本", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(challengeVersionService.deleteWithValidByIds(List.of(ids), true));
    }
}

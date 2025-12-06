package com.kdajv.cch.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.kdajv.cch.domain.bo.ChallengeDraftBo;
import com.kdajv.cch.domain.vo.ChallengeDraftVo;
import com.kdajv.cch.service.IChallengeDraftService;
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
 * 题目草稿
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/cch/challengeDraft")
public class ChallengeDraftController extends BaseController {

    private final IChallengeDraftService challengeDraftService;

    /**
     * 查询题目草稿列表
     */
    @SaCheckPermission("cch:challengeDraft:list")
    @GetMapping("/list")
    public TableDataInfo<ChallengeDraftVo> list(ChallengeDraftBo bo, PageQuery pageQuery) {
        return challengeDraftService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出题目草稿列表
     */
    @SaCheckPermission("cch:challengeDraft:export")
    @Log(title = "题目草稿", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ChallengeDraftBo bo, HttpServletResponse response) {
        List<ChallengeDraftVo> list = challengeDraftService.queryList(bo);
        ExcelUtil.exportExcel(list, "题目草稿", ChallengeDraftVo.class, response);
    }

    /**
     * 获取题目草稿详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("cch:challengeDraft:query")
    @GetMapping("/{id}")
    public R<ChallengeDraftVo> getInfo(@NotNull(message = "主键不能为空")
                                       @PathVariable Long id) {
        return R.ok(challengeDraftService.queryById(id));
    }

    /**
     * 新增题目草稿
     */
    @SaCheckPermission("cch:challengeDraft:add")
    @Log(title = "题目草稿", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ChallengeDraftBo bo) {
        return toAjax(challengeDraftService.insertByBo(bo));
    }

    /**
     * 修改题目草稿
     */
    @SaCheckPermission("cch:challengeDraft:edit")
    @Log(title = "题目草稿", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChallengeDraftBo bo) {
        return toAjax(challengeDraftService.updateByBo(bo));
    }

    /**
     * 删除题目草稿
     *
     * @param ids 主键串
     */
    @SaCheckPermission("cch:challengeDraft:remove")
    @Log(title = "题目草稿", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(challengeDraftService.deleteWithValidByIds(List.of(ids), true));
    }
}

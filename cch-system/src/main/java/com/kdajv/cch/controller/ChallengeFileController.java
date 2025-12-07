package com.kdajv.cch.controller;

import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import com.kdajv.cch.domain.vo.ChallengeVo;
import com.kdajv.cch.service.IChallengeService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.service.OssService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;
import com.kdajv.cch.domain.vo.ChallengeFileVo;
import com.kdajv.cch.domain.bo.ChallengeFileBo;
import com.kdajv.cch.service.IChallengeFileService;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 题目文件
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/cch/challengeFile")
public class ChallengeFileController extends BaseController {

    private final IChallengeService challengeService;
    private final IChallengeFileService challengeFileService;

    /**
     * 查询题目文件列表
     */
    @SaCheckPermission("cch:challengeFile:list")
    @GetMapping("/list")
    public TableDataInfo<ChallengeFileVo> list(ChallengeFileBo bo, PageQuery pageQuery) {
        return challengeFileService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出题目文件列表
     */
    @SaCheckPermission("cch:challengeFile:export")
    @Log(title = "题目文件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ChallengeFileBo bo, HttpServletResponse response) {
        List<ChallengeFileVo> list = challengeFileService.queryList(bo);
        ExcelUtil.exportExcel(list, "题目文件", ChallengeFileVo.class, response);
    }

    /**
     * 获取题目文件详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("cch:challengeFile:query")
    @GetMapping("/{id}")
    public R<ChallengeFileVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(challengeFileService.queryById(id));
    }

    /**
     * 新增题目文件
     */
    @SaCheckPermission("cch:challengeFile:add")
    @Log(title = "题目文件", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ChallengeFileBo bo) {
        return toAjax(challengeFileService.insertByBo(bo));
    }

    /**
     * 修改题目文件
     */
    @SaCheckPermission("cch:challengeFile:edit")
    @Log(title = "题目文件", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChallengeFileBo bo) {
        return toAjax(challengeFileService.updateByBo(bo));
    }

    /**
     * 删除题目文件
     *
     * @param ids 主键串
     */
    @SaCheckPermission("cch:challengeFile:remove")
    @Log(title = "题目文件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(challengeFileService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 上传附件或Writeup
     *
     * @param file 文件
     */
    @SaCheckPermission("cch:challengeFile:add")
    @Log(title = "题目文件", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<ChallengeFileVo> upload(Long challengeId, @RequestPart("file") MultipartFile file) {
        if (ObjectUtil.isNull(file)) {
            return R.fail("上传文件不能为空");
        }
        if (ObjectUtil.isNull(challengeService.queryById(challengeId))) {
            return R.fail("题目不存在");
        }
        ChallengeFileVo vo = challengeFileService.upload(challengeId, file);
        return R.ok(vo);
    }

}

package com.kdajv.cch.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kdajv.cch.service.ImageUploadService;
import jakarta.validation.constraints.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.domain.R;
import com.kdajv.cch.domain.vo.ChallengeContainerImageVo;
import com.kdajv.cch.domain.bo.ChallengeContainerImageBo;
import com.kdajv.cch.service.IChallengeContainerImageService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 挑战容器镜像Controller
 *
 * @author Lingma
 * @date 2026-01-27
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/cch/challengeContainerImage")
public class ChallengeContainerImageController extends BaseController {

    private final IChallengeContainerImageService challengeContainerImageService;
    private final ImageUploadService imageUploadService;

    /**
     * 查询挑战容器镜像列表
     */
    @SaCheckPermission("cch:challengeContainerImage:list")
    @GetMapping("/list")
    public TableDataInfo<ChallengeContainerImageVo> list(ChallengeContainerImageBo bo, PageQuery pageQuery) {
        return challengeContainerImageService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出挑战容器镜像列表
     */
    @SaCheckPermission("cch:challengeContainerImage:export")
    @Log(title = "挑战容器镜像", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ChallengeContainerImageBo bo, HttpServletResponse response) {
        List<ChallengeContainerImageVo> list = challengeContainerImageService.queryList(bo);
        ExcelUtil.exportExcel(list, "挑战容器镜像", ChallengeContainerImageVo.class, response);
    }

    /**
     * 获取挑战容器镜像详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("cch:challengeContainerImage:query")
    @GetMapping("/{id}")
    public R<ChallengeContainerImageVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(challengeContainerImageService.queryById(id));
    }

    /**
     * 新增挑战容器镜像
     */
    @SaCheckPermission("cch:challengeContainerImage:add")
    @Log(title = "挑战容器镜像", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated @RequestBody ChallengeContainerImageBo bo) {
        return toAjax(challengeContainerImageService.insertByBo(bo));
    }

    /**
     * 修改挑战容器镜像
     */
    @SaCheckPermission("cch:challengeContainerImage:edit")
    @Log(title = "挑战容器镜像", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated @RequestBody ChallengeContainerImageBo bo) {
        return toAjax(challengeContainerImageService.updateByBo(bo));
    }

    /**
     * 删除挑战容器镜像
     *
     * @param ids 主键串
     */
    @SaCheckPermission("cch:challengeContainerImage:remove")
    @Log(title = "挑战容器镜像", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(challengeContainerImageService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 上传容器镜像
     *
     * @param challengeId 题目ID
     * @param file        镜像文件
     */
    @SaCheckPermission("cch:challengeContainerImage:add")
    @Log(title = "挑战容器镜像", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/upload")
    public R<ChallengeContainerImageVo> uploadImage(@RequestParam("challengeId") Long challengeId, @RequestParam("imageName") String imageName, @RequestParam("file") MultipartFile file) {
        try {
            ChallengeContainerImageVo vo = imageUploadService.uploadImage(challengeId, imageName, file);
            return R.ok(vo);
        } catch (Exception e) {
            log.error("上传镜像失败", e);
            return R.fail("上传失败: " + e.getMessage());
        }
    }

    /**
     * 根据题目ID查询镜像列表
     *
     * @param challengeId 题目ID
     */
    @SaCheckPermission("cch:challengeContainerImage:list")
    @GetMapping("/byChallenge/{challengeId}")
    public R<List<ChallengeContainerImageVo>> getByChallengeId(@PathVariable Long challengeId) {
        List<ChallengeContainerImageVo> list = challengeContainerImageService.getByChallengeId(challengeId);
        return R.ok(list);
    }

    /**
     * 获取上传进度
     *
     * @param id 镜像记录ID
     */
    @SaCheckPermission("cch:challengeContainerImage:query")
    @GetMapping("/progress/{id}")
    public R<Map<String, Object>> getUploadProgress(@PathVariable Long id) {
        Double progress = imageUploadService.getUploadProgress(id);
        if (progress == null) {
            return R.fail("镜像记录不存在或进度信息无效");
        }

        ChallengeContainerImageVo image = challengeContainerImageService.queryById(id);
        if (image == null) {
            return R.fail("镜像记录不存在");
        }

        Map<String, Object> progressInfo = new HashMap<>();
        progressInfo.put("id", image.getId());
        progressInfo.put("status", image.getStatus());
        progressInfo.put("progress", progress);
        progressInfo.put("errorMessage", image.getErrorMessage());
        
        return R.ok(progressInfo);
    }

    /**
     * 手动Load镜像到Docker
     *
     * @param id 镜像记录ID
     */
    @SaCheckPermission("cch:challengeContainerImage:edit")
    @Log(title = "挑战容器镜像", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/load/{id}")
    public R<Void> loadToDocker(@PathVariable Long id) {
        try {
            Boolean result = imageUploadService.manualLoadImage(id);
            if (result) {
                return R.ok("Load镜像成功");
            } else {
                return R.fail("Load镜像失败");
            }
        } catch (Exception e) {
            log.error("Load镜像失败", e);
            return R.fail("Load镜像失败: " + e.getMessage());
        }
    }
}

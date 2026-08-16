package com.kdajv.cch.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.kdajv.cch.domain.DraftConfig;
import com.kdajv.cch.domain.bo.ChallengeBo;
import com.kdajv.cch.domain.bo.ChallengeDraftBo;
import com.kdajv.cch.domain.vo.ChallengeDraftVo;
import com.kdajv.cch.domain.vo.ChallengeVo;
import com.kdajv.cch.service.IChallengeDraftService;
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
    private final IChallengeDraftService challengeDraftService;

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
    public R<ChallengeVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(challengeService.queryById(id));
    }

    /**
     * 获取题目的草稿信息
     *
     * @param id 题目ID
     */
    @SaCheckPermission({"cch:challenge:edit", "cch:challengeDraft:query", "cch:challengeDraft:add", "cch:challengeDraft:edit"})
    @GetMapping("/{id}/draft")
    public R<ChallengeDraftVo> draft(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        // 查找最新的草稿
        ChallengeDraftVo latestDraft = challengeDraftService.queryTop1ByChallengeIdOrderByCreateTimeDesc(id);
        // 如果没有找到草稿，则创建新的草稿
        if (latestDraft == null) {
            ChallengeVo challenge = challengeService.queryById(id);
            if (challenge != null) {
                ChallengeDraftBo draftBo = new ChallengeDraftBo();
                draftBo.setChallengeId(challenge.getId());
                draftBo.setChallengeName(challenge.getName());
                draftBo.setConfig(new DraftConfig());
                challengeDraftService.insertByBo(draftBo);
                // 重新查询确保获取完整信息
                ChallengeDraftVo newDraft = challengeDraftService.queryById(draftBo.getId());
                return R.ok(newDraft);
            } else {
                return R.fail("找不到指定的题目");
            }
        } else {
            // 返回找到的草稿
            return R.ok(latestDraft);
        }
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
     * 初始化一道新题目（含首个草稿）
     * <p>
     * 供"新增题目入库"一体化流程使用：一步完成 t_challenge 与首个 t_challenge_draft 的创建，
     * 返回草稿信息后前端直接进入草稿编辑页继续完善内容。
     *
     * @param bo 题目基础信息 + 初始草稿配置
     */
    @SaCheckPermission("cch:challenge:add")
    @Log(title = "题目列表", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/init")
    public R<ChallengeDraftVo> init(@RequestBody ChallengeDraftBo bo) {
        return R.ok(challengeService.initChallengeWithDraft(bo));
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
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(challengeService.deleteWithValidByIds(List.of(ids), true));
    }
}

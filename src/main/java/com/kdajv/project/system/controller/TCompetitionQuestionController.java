package com.kdajv.project.system.controller;

import com.kdajv.common.utils.poi.ExcelUtil;
import com.kdajv.framework.aspectj.lang.annotation.Log;
import com.kdajv.framework.aspectj.lang.enums.BusinessType;
import com.kdajv.framework.web.controller.BaseController;
import com.kdajv.framework.web.domain.AjaxResult;
import com.kdajv.framework.web.page.TableDataInfo;
import com.kdajv.project.system.domain.TCompetitionQuestion;
import com.kdajv.project.system.service.ITCompetitionQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 竞赛题目Controller
 * 
 * @author GZY
 * @date 2024-12-25
 */
@RestController
@RequestMapping("/exam/competitionQuestion")
public class TCompetitionQuestionController extends BaseController
{
    @Autowired
    private ITCompetitionQuestionService tCompetitionQuestionService;

    /**
     * 查询竞赛题目列表
     */
    @PreAuthorize("@ss.hasPermi('exam:competitionQuestion:list')")
    @GetMapping("/list")
    public TableDataInfo list(TCompetitionQuestion tCompetitionQuestion)
    {
        startPage();
        List<TCompetitionQuestion> list = tCompetitionQuestionService.selectTCompetitionQuestionList(tCompetitionQuestion);
        return getDataTable(list);
    }

    /**
     * 导出竞赛题目列表
     */
    @PreAuthorize("@ss.hasPermi('exam:competitionQuestion:export')")
    @Log(title = "竞赛题目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TCompetitionQuestion tCompetitionQuestion)
    {
        List<TCompetitionQuestion> list = tCompetitionQuestionService.selectTCompetitionQuestionList(tCompetitionQuestion);
        ExcelUtil<TCompetitionQuestion> util = new ExcelUtil<TCompetitionQuestion>(TCompetitionQuestion.class);
        util.exportExcel(response, list, "竞赛题目数据");
    }

    /**
     * 获取竞赛题目详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:competitionQuestion:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tCompetitionQuestionService.selectTCompetitionQuestionById(id));
    }

    /**
     * 新增竞赛题目
     */
    @PreAuthorize("@ss.hasPermi('exam:competitionQuestion:add')")
    @Log(title = "竞赛题目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TCompetitionQuestion tCompetitionQuestion)
    {
        return toAjax(tCompetitionQuestionService.insertTCompetitionQuestion(tCompetitionQuestion));
    }

    /**
     * 修改竞赛题目
     */
    @PreAuthorize("@ss.hasPermi('exam:competitionQuestion:edit')")
    @Log(title = "竞赛题目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TCompetitionQuestion tCompetitionQuestion)
    {
        return toAjax(tCompetitionQuestionService.updateTCompetitionQuestion(tCompetitionQuestion));
    }

    /**
     * 删除竞赛题目
     */
    @PreAuthorize("@ss.hasPermi('exam:competitionQuestion:remove')")
    @Log(title = "竞赛题目", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tCompetitionQuestionService.deleteTCompetitionQuestionByIds(ids));
    }
}

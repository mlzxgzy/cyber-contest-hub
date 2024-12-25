package com.kdajv.project.system.controller;

import com.kdajv.common.utils.poi.ExcelUtil;
import com.kdajv.framework.aspectj.lang.annotation.Log;
import com.kdajv.framework.aspectj.lang.enums.BusinessType;
import com.kdajv.framework.web.controller.BaseController;
import com.kdajv.framework.web.domain.AjaxResult;
import com.kdajv.framework.web.page.TableDataInfo;
import com.kdajv.project.system.domain.TQuestionTag;
import com.kdajv.project.system.service.ITQuestionTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 题目TagController
 * 
 * @author GZY
 * @date 2024-12-25
 */
@RestController
@RequestMapping("/exam/questionTag")
public class TQuestionTagController extends BaseController
{
    @Autowired
    private ITQuestionTagService tQuestionTagService;

    /**
     * 查询题目Tag列表
     */
    @PreAuthorize("@ss.hasPermi('exam:questionTag:list')")
    @GetMapping("/list")
    public TableDataInfo list(TQuestionTag tQuestionTag)
    {
        startPage();
        List<TQuestionTag> list = tQuestionTagService.selectTQuestionTagList(tQuestionTag);
        return getDataTable(list);
    }

    /**
     * 导出题目Tag列表
     */
    @PreAuthorize("@ss.hasPermi('exam:questionTag:export')")
    @Log(title = "题目Tag", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TQuestionTag tQuestionTag)
    {
        List<TQuestionTag> list = tQuestionTagService.selectTQuestionTagList(tQuestionTag);
        ExcelUtil<TQuestionTag> util = new ExcelUtil<TQuestionTag>(TQuestionTag.class);
        util.exportExcel(response, list, "题目Tag数据");
    }

    /**
     * 获取题目Tag详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:questionTag:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tQuestionTagService.selectTQuestionTagById(id));
    }

    /**
     * 新增题目Tag
     */
    @PreAuthorize("@ss.hasPermi('exam:questionTag:add')")
    @Log(title = "题目Tag", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TQuestionTag tQuestionTag)
    {
        return toAjax(tQuestionTagService.insertTQuestionTag(tQuestionTag));
    }

    /**
     * 修改题目Tag
     */
    @PreAuthorize("@ss.hasPermi('exam:questionTag:edit')")
    @Log(title = "题目Tag", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TQuestionTag tQuestionTag)
    {
        return toAjax(tQuestionTagService.updateTQuestionTag(tQuestionTag));
    }

    /**
     * 删除题目Tag
     */
    @PreAuthorize("@ss.hasPermi('exam:questionTag:remove')")
    @Log(title = "题目Tag", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tQuestionTagService.deleteTQuestionTagByIds(ids));
    }
}

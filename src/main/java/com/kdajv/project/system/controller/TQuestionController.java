package com.kdajv.project.system.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kdajv.framework.aspectj.lang.annotation.Log;
import com.kdajv.framework.aspectj.lang.enums.BusinessType;
import com.kdajv.project.system.domain.TQuestion;
import com.kdajv.project.system.service.ITQuestionService;
import com.kdajv.framework.web.controller.BaseController;
import com.kdajv.framework.web.domain.AjaxResult;
import com.kdajv.common.utils.poi.ExcelUtil;
import com.kdajv.framework.web.page.TableDataInfo;

/**
 * 题目Controller
 * 
 * @author GZY
 * @date 2024-12-18
 */
@RestController
@RequestMapping("/exam/question")
public class TQuestionController extends BaseController
{
    @Autowired
    private ITQuestionService tQuestionService;

    /**
     * 查询题目列表
     */
    @PreAuthorize("@ss.hasPermi('exam:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(TQuestion tQuestion)
    {
        startPage();
        List<TQuestion> list = tQuestionService.selectTQuestionList(tQuestion);
        return getDataTable(list);
    }

    /**
     * 导出题目列表
     */
    @PreAuthorize("@ss.hasPermi('exam:question:export')")
    @Log(title = "题目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TQuestion tQuestion)
    {
        List<TQuestion> list = tQuestionService.selectTQuestionList(tQuestion);
        ExcelUtil<TQuestion> util = new ExcelUtil<TQuestion>(TQuestion.class);
        util.exportExcel(response, list, "题目数据");
    }

    /**
     * 获取题目详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:question:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tQuestionService.selectTQuestionById(id));
    }

    /**
     * 新增题目
     */
    @PreAuthorize("@ss.hasPermi('exam:question:add')")
    @Log(title = "题目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TQuestion tQuestion)
    {
        return toAjax(tQuestionService.insertTQuestion(tQuestion));
    }

    /**
     * 修改题目
     */
    @PreAuthorize("@ss.hasPermi('exam:question:edit')")
    @Log(title = "题目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TQuestion tQuestion)
    {
        return toAjax(tQuestionService.updateTQuestion(tQuestion));
    }

    /**
     * 删除题目
     */
    @PreAuthorize("@ss.hasPermi('exam:question:remove')")
    @Log(title = "题目", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tQuestionService.deleteTQuestionByIds(ids));
    }
}

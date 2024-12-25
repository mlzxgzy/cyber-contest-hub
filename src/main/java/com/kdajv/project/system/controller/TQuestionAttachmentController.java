package com.kdajv.project.system.controller;

import com.kdajv.common.utils.poi.ExcelUtil;
import com.kdajv.framework.aspectj.lang.annotation.Log;
import com.kdajv.framework.aspectj.lang.enums.BusinessType;
import com.kdajv.framework.web.controller.BaseController;
import com.kdajv.framework.web.domain.AjaxResult;
import com.kdajv.framework.web.page.TableDataInfo;
import com.kdajv.project.system.domain.TQuestionAttachment;
import com.kdajv.project.system.service.ITQuestionAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 题目附件Controller
 * 
 * @author GZY
 * @date 2024-12-25
 */
@RestController
@RequestMapping("/exam/questionAttachment")
public class TQuestionAttachmentController extends BaseController
{
    @Autowired
    private ITQuestionAttachmentService tQuestionAttachmentService;

    /**
     * 查询题目附件列表
     */
    @PreAuthorize("@ss.hasPermi('exam:questionAttachment:list')")
    @GetMapping("/list")
    public TableDataInfo list(TQuestionAttachment tQuestionAttachment)
    {
        startPage();
        List<TQuestionAttachment> list = tQuestionAttachmentService.selectTQuestionAttachmentList(tQuestionAttachment);
        return getDataTable(list);
    }

    /**
     * 导出题目附件列表
     */
    @PreAuthorize("@ss.hasPermi('exam:questionAttachment:export')")
    @Log(title = "题目附件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TQuestionAttachment tQuestionAttachment)
    {
        List<TQuestionAttachment> list = tQuestionAttachmentService.selectTQuestionAttachmentList(tQuestionAttachment);
        ExcelUtil<TQuestionAttachment> util = new ExcelUtil<TQuestionAttachment>(TQuestionAttachment.class);
        util.exportExcel(response, list, "题目附件数据");
    }

    /**
     * 获取题目附件详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:questionAttachment:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tQuestionAttachmentService.selectTQuestionAttachmentById(id));
    }

    /**
     * 新增题目附件
     */
    @PreAuthorize("@ss.hasPermi('exam:questionAttachment:add')")
    @Log(title = "题目附件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TQuestionAttachment tQuestionAttachment)
    {
        return toAjax(tQuestionAttachmentService.insertTQuestionAttachment(tQuestionAttachment));
    }

    /**
     * 修改题目附件
     */
    @PreAuthorize("@ss.hasPermi('exam:questionAttachment:edit')")
    @Log(title = "题目附件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TQuestionAttachment tQuestionAttachment)
    {
        return toAjax(tQuestionAttachmentService.updateTQuestionAttachment(tQuestionAttachment));
    }

    /**
     * 删除题目附件
     */
    @PreAuthorize("@ss.hasPermi('exam:questionAttachment:remove')")
    @Log(title = "题目附件", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tQuestionAttachmentService.deleteTQuestionAttachmentByIds(ids));
    }
}

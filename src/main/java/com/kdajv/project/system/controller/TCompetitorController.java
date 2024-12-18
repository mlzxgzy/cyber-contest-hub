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
import com.kdajv.project.system.domain.TCompetitor;
import com.kdajv.project.system.service.ITCompetitorService;
import com.kdajv.framework.web.controller.BaseController;
import com.kdajv.framework.web.domain.AjaxResult;
import com.kdajv.common.utils.poi.ExcelUtil;
import com.kdajv.framework.web.page.TableDataInfo;

/**
 * 选手Controller
 * 
 * @author GZY
 * @date 2024-12-18
 */
@RestController
@RequestMapping("/exam/competitor")
public class TCompetitorController extends BaseController
{
    @Autowired
    private ITCompetitorService tCompetitorService;

    /**
     * 查询选手列表
     */
    @PreAuthorize("@ss.hasPermi('exam:competitor:list')")
    @GetMapping("/list")
    public TableDataInfo list(TCompetitor tCompetitor)
    {
        startPage();
        List<TCompetitor> list = tCompetitorService.selectTCompetitorList(tCompetitor);
        return getDataTable(list);
    }

    /**
     * 导出选手列表
     */
    @PreAuthorize("@ss.hasPermi('exam:competitor:export')")
    @Log(title = "选手", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TCompetitor tCompetitor)
    {
        List<TCompetitor> list = tCompetitorService.selectTCompetitorList(tCompetitor);
        ExcelUtil<TCompetitor> util = new ExcelUtil<TCompetitor>(TCompetitor.class);
        util.exportExcel(response, list, "选手数据");
    }

    /**
     * 获取选手详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:competitor:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tCompetitorService.selectTCompetitorById(id));
    }

    /**
     * 新增选手
     */
    @PreAuthorize("@ss.hasPermi('exam:competitor:add')")
    @Log(title = "选手", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TCompetitor tCompetitor)
    {
        return toAjax(tCompetitorService.insertTCompetitor(tCompetitor));
    }

    /**
     * 修改选手
     */
    @PreAuthorize("@ss.hasPermi('exam:competitor:edit')")
    @Log(title = "选手", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TCompetitor tCompetitor)
    {
        return toAjax(tCompetitorService.updateTCompetitor(tCompetitor));
    }

    /**
     * 删除选手
     */
    @PreAuthorize("@ss.hasPermi('exam:competitor:remove')")
    @Log(title = "选手", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tCompetitorService.deleteTCompetitorByIds(ids));
    }
}

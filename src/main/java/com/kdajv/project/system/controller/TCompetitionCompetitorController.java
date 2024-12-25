package com.kdajv.project.system.controller;

import com.kdajv.common.utils.poi.ExcelUtil;
import com.kdajv.framework.aspectj.lang.annotation.Log;
import com.kdajv.framework.aspectj.lang.enums.BusinessType;
import com.kdajv.framework.web.controller.BaseController;
import com.kdajv.framework.web.domain.AjaxResult;
import com.kdajv.framework.web.page.TableDataInfo;
import com.kdajv.project.system.domain.TCompetitionCompetitor;
import com.kdajv.project.system.service.ITCompetitionCompetitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 竞赛选手Controller
 * 
 * @author GZY
 * @date 2024-12-25
 */
@RestController
@RequestMapping("/exam/competiitonCompetitor")
public class TCompetitionCompetitorController extends BaseController
{
    @Autowired
    private ITCompetitionCompetitorService tCompetitionCompetitorService;

    /**
     * 查询竞赛选手列表
     */
    @PreAuthorize("@ss.hasPermi('exam:competiitonCompetitor:list')")
    @GetMapping("/list")
    public TableDataInfo list(TCompetitionCompetitor tCompetitionCompetitor)
    {
        startPage();
        List<TCompetitionCompetitor> list = tCompetitionCompetitorService.selectTCompetitionCompetitorList(tCompetitionCompetitor);
        return getDataTable(list);
    }

    /**
     * 导出竞赛选手列表
     */
    @PreAuthorize("@ss.hasPermi('exam:competiitonCompetitor:export')")
    @Log(title = "竞赛选手", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TCompetitionCompetitor tCompetitionCompetitor)
    {
        List<TCompetitionCompetitor> list = tCompetitionCompetitorService.selectTCompetitionCompetitorList(tCompetitionCompetitor);
        ExcelUtil<TCompetitionCompetitor> util = new ExcelUtil<TCompetitionCompetitor>(TCompetitionCompetitor.class);
        util.exportExcel(response, list, "竞赛选手数据");
    }

    /**
     * 获取竞赛选手详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:competiitonCompetitor:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tCompetitionCompetitorService.selectTCompetitionCompetitorById(id));
    }

    /**
     * 新增竞赛选手
     */
    @PreAuthorize("@ss.hasPermi('exam:competiitonCompetitor:add')")
    @Log(title = "竞赛选手", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TCompetitionCompetitor tCompetitionCompetitor)
    {
        return toAjax(tCompetitionCompetitorService.insertTCompetitionCompetitor(tCompetitionCompetitor));
    }

    /**
     * 修改竞赛选手
     */
    @PreAuthorize("@ss.hasPermi('exam:competiitonCompetitor:edit')")
    @Log(title = "竞赛选手", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TCompetitionCompetitor tCompetitionCompetitor)
    {
        return toAjax(tCompetitionCompetitorService.updateTCompetitionCompetitor(tCompetitionCompetitor));
    }

    /**
     * 删除竞赛选手
     */
    @PreAuthorize("@ss.hasPermi('exam:competiitonCompetitor:remove')")
    @Log(title = "竞赛选手", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tCompetitionCompetitorService.deleteTCompetitionCompetitorByIds(ids));
    }
}

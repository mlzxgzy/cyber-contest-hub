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
import com.kdajv.project.system.domain.TCompetition;
import com.kdajv.project.system.service.ITCompetitionService;
import com.kdajv.framework.web.controller.BaseController;
import com.kdajv.framework.web.domain.AjaxResult;
import com.kdajv.common.utils.poi.ExcelUtil;
import com.kdajv.framework.web.page.TableDataInfo;

/**
 * 竞赛Controller
 * 
 * @author GZY
 * @date 2024-12-18
 */
@RestController
@RequestMapping("/exam/competition")
public class TCompetitionController extends BaseController
{
    @Autowired
    private ITCompetitionService tCompetitionService;

    /**
     * 查询竞赛列表
     */
    @PreAuthorize("@ss.hasPermi('exam:competition:list')")
    @GetMapping("/list")
    public TableDataInfo list(TCompetition tCompetition)
    {
        startPage();
        List<TCompetition> list = tCompetitionService.selectTCompetitionList(tCompetition);
        return getDataTable(list);
    }

    /**
     * 导出竞赛列表
     */
    @PreAuthorize("@ss.hasPermi('exam:competition:export')")
    @Log(title = "竞赛", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TCompetition tCompetition)
    {
        List<TCompetition> list = tCompetitionService.selectTCompetitionList(tCompetition);
        ExcelUtil<TCompetition> util = new ExcelUtil<TCompetition>(TCompetition.class);
        util.exportExcel(response, list, "竞赛数据");
    }

    /**
     * 获取竞赛详细信息
     */
    @PreAuthorize("@ss.hasPermi('exam:competition:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tCompetitionService.selectTCompetitionById(id));
    }

    /**
     * 新增竞赛
     */
    @PreAuthorize("@ss.hasPermi('exam:competition:add')")
    @Log(title = "竞赛", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TCompetition tCompetition)
    {
        return toAjax(tCompetitionService.insertTCompetition(tCompetition));
    }

    /**
     * 修改竞赛
     */
    @PreAuthorize("@ss.hasPermi('exam:competition:edit')")
    @Log(title = "竞赛", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TCompetition tCompetition)
    {
        return toAjax(tCompetitionService.updateTCompetition(tCompetition));
    }

    /**
     * 删除竞赛
     */
    @PreAuthorize("@ss.hasPermi('exam:competition:remove')")
    @Log(title = "竞赛", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tCompetitionService.deleteTCompetitionByIds(ids));
    }
}

package com.kdajv.cch.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.kdajv.cch.domain.vo.CchContainerConfigVo;
import com.kdajv.cch.service.ICchContainerConfigService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 容器配置 信息操作处理
 *
 * @author system
 * @date 2025-12-11
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/cch/container/config")
public class CchContainerConfigController extends BaseController {

    private final ICchContainerConfigService containerConfigService;

    /**
     * 获取容器配置列表
     */
    @SaCheckPermission("container:config:list")
    @GetMapping("/list")
    public TableDataInfo<CchContainerConfigVo> list(@RequestParam(required = false) String configName, @RequestParam(required = false) String backendType, PageQuery pageQuery) {
        return containerConfigService.queryPageList(configName, backendType, pageQuery);
    }

    /**
     * 导出容器配置列表
     */
    @Log(title = "容器配置", businessType = BusinessType.EXPORT)
    @SaCheckPermission("container:config:export")
    @PostMapping("/export")
    public void export(@RequestParam(required = false) String configName, @RequestParam(required = false) String backendType, HttpServletResponse response) {
        List<CchContainerConfigVo> list = containerConfigService.queryList(configName, backendType);
        ExcelUtil.exportExcel(list, "容器配置数据", CchContainerConfigVo.class, response);
    }

    /**
     * 根据容器配置编号获取详细信息
     *
     * @param id 容器配置ID
     */
    @SaCheckPermission("container:config:query")
    @GetMapping(value = "/{id}")
    public R<CchContainerConfigVo> getInfo(@PathVariable Long id) {
        return R.ok(containerConfigService.queryById(id));
    }

    /**
     * 新增容器配置
     */
    @SaCheckPermission("container:config:add")
    @Log(title = "容器配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody CchContainerConfigVo vo) {
        return toAjax(containerConfigService.insertByVo(vo));
    }

    /**
     * 修改容器配置
     */
    @SaCheckPermission("container:config:edit")
    @Log(title = "容器配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody CchContainerConfigVo vo) {
        return toAjax(containerConfigService.updateByVo(vo));
    }

    /**
     * 删除容器配置
     *
     * @param ids 容器配置ID串
     */
    @SaCheckPermission("container:config:remove")
    @Log(title = "容器配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(containerConfigService.deleteByIds(Arrays.asList(ids)));
    }

    /**
     * 测试连接
     *
     * @param id 容器配置ID
     */
    @SaCheckPermission("container:config:test")
    @Log(title = "容器配置", businessType = BusinessType.OTHER)
    @PostMapping("/test/{id}")
    public R<Void> testConnection(@PathVariable Long id) {
        return toAjax(containerConfigService.testConnection(id));
    }

    /**
     * 获取当前活跃的容器实例
     */
    @SaCheckPermission("container:config:query")
    @GetMapping("/active")
    public R<CchContainerConfigVo> getActiveInstance() {
        return R.ok(containerConfigService.getActiveInstance());
    }

    /**
     * 断开当前活跃的容器实例
     */
    @SaCheckPermission("container:config:edit")
    @Log(title = "容器配置", businessType = BusinessType.OTHER)
    @PostMapping("/disconnect")
    public R<Void> disconnect() {
        containerConfigService.disconnectActiveInstance();
        return R.ok();
    }

}

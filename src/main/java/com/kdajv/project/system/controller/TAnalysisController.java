package com.kdajv.project.system.controller;

import com.kdajv.framework.web.controller.BaseController;
import com.kdajv.framework.web.domain.AjaxResult;
import com.kdajv.project.system.service.ITAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/exam/analysis")
public class TAnalysisController extends BaseController {

    @Autowired
    private ITAnalysisService analysisService;

    @GetMapping("/data")
    public AjaxResult getAnalysisData() {
        Map<String, Object> result = analysisService.getAnalysisData();
        return AjaxResult.success(result);
    }
}
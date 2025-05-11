package com.kdajv.project.system.service.impl;


import com.kdajv.project.system.mapper.TCompetitionCompetitorMapper;
import com.kdajv.project.system.mapper.TCompetitionMapper;
import com.kdajv.project.system.mapper.TCompetitorMapper;
import com.kdajv.project.system.mapper.TQuestionMapper;
import com.kdajv.project.system.service.ITAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class TAnalysisServiceImpl implements ITAnalysisService {

    @Autowired
    private TCompetitionMapper competitionMapper;

    @Autowired
    private TCompetitorMapper competitorMapper;

    @Autowired
    private TQuestionMapper questionMapper;

    @Autowired
    private TCompetitionCompetitorMapper competitionCompetitorMapper;

    @Override
    public Map<String, Object> getAnalysisData() {
        Map<String, Object> result = new HashMap<>();

        // 基础统计
        result.put("stats", getBasicStats());

        // 题目类型分布
        result.put("typeDistribution", questionMapper.countByCategory());

        // 竞赛时间分布（按月份）
        result.put("timeDistribution", competitionMapper.countByMonth());

        // 题目难度分布
        result.put("difficultyDistribution", questionMapper.countByDifficulty());

        // 活跃选手TOP10
        result.put("activeCompetitors", competitionCompetitorMapper.getActiveCompetitorsTop10());

        return result;
    }

    private Map<String, Object> getBasicStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("competitionCount", competitionMapper.selectCount());
        stats.put("competitorCount", competitorMapper.selectCount());
        stats.put("questionCount", questionMapper.selectCount());
        stats.put("avgDifficulty", questionMapper.getAvgDifficulty());
        return stats;
    }
}

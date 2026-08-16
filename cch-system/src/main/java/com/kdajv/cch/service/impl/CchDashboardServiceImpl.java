package com.kdajv.cch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kdajv.cch.domain.*;
import com.kdajv.cch.domain.vo.CchDashboardVo;
import com.kdajv.cch.domain.vo.ProjectVo;
import com.kdajv.cch.mapper.*;
import com.kdajv.cch.service.ICchDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 首页仪表盘Service业务层处理
 *
 * @author system
 * @date 2026-03-21
 */
@RequiredArgsConstructor
@Service
public class CchDashboardServiceImpl implements ICchDashboardService {

    private final ProjectMapper projectMapper;
    private final ChallengeMapper challengeMapper;
    private final ChallengeVersionMapper challengeVersionMapper;
    private final ChallengeDraftMapper challengeDraftMapper;
    private final ChallengeFileMapper challengeFileMapper;
    private final ChallengeContainerImageMapper challengeContainerImageMapper;
    private final ChallengeContainerMockTestMapper challengeContainerMockTestMapper;
    private final ChallengeVersionExportTaskMapper challengeVersionExportTaskMapper;
    private final ProjectChallengeMapper projectChallengeMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ContestFileMapper contestFileMapper;

    @Override
    public CchDashboardVo getDashboardStatistics() {
        CchDashboardVo vo = new CchDashboardVo();
        vo.setOverview(buildOverview());
        vo.setCategoryDistribution(buildNameValueDistribution(
            challengeMapper.selectList(new LambdaQueryWrapper<Challenge>().select(Challenge::getCategory)),
            Challenge::getCategory,
            "未分类"
        ));
        vo.setProjectTypeDistribution(buildNameValueDistribution(
            projectMapper.selectList(new LambdaQueryWrapper<Project>().select(Project::getProjectType)),
            Project::getProjectType,
            "normal"
        ));
        vo.setImageStatusDistribution(buildNameValueDistribution(
            challengeContainerImageMapper.selectList(new LambdaQueryWrapper<ChallengeContainerImage>().select(ChallengeContainerImage::getStatus)),
            ChallengeContainerImage::getStatus,
            "unknown"
        ));
        vo.setExportTaskStatusDistribution(buildNameValueDistribution(
            challengeVersionExportTaskMapper.selectList(new LambdaQueryWrapper<ChallengeVersionExportTask>().select(ChallengeVersionExportTask::getTaskStatus)),
            task -> task.getTaskStatus() == null ? "unknown" : String.valueOf(task.getTaskStatus()),
            "unknown"
        ));
        vo.setTrend(buildTrend());
        vo.setRecentProjects(buildRecentProjects());
        return vo;
    }

    private CchDashboardVo.Overview buildOverview() {
        CchDashboardVo.Overview overview = new CchDashboardVo.Overview();
        overview.setProjectCount(projectMapper.selectCount(new LambdaQueryWrapper<Project>()));
        overview.setChallengeCount(challengeMapper.selectCount(new LambdaQueryWrapper<Challenge>()));
        overview.setVersionCount(challengeVersionMapper.selectCount(new LambdaQueryWrapper<ChallengeVersion>()));
        overview.setDraftCount(challengeDraftMapper.selectCount(new LambdaQueryWrapper<ChallengeDraft>()));
        overview.setFileCount(challengeFileMapper.selectCount(new LambdaQueryWrapper<ChallengeFile>()));
        overview.setImageCount(challengeContainerImageMapper.selectCount(new LambdaQueryWrapper<ChallengeContainerImage>()));
        overview.setMockTestCount(challengeContainerMockTestMapper.selectCount(new LambdaQueryWrapper<ChallengeContainerMockTest>()));
        overview.setExportTaskCount(challengeVersionExportTaskMapper.selectCount(new LambdaQueryWrapper<ChallengeVersionExportTask>()));
        overview.setProjectChallengeCount(projectChallengeMapper.selectCount(new LambdaQueryWrapper<ProjectChallenge>()));
        overview.setProjectMemberCount(projectMemberMapper.selectCount(new LambdaQueryWrapper<ProjectMember>()));
        overview.setContestFileCount(contestFileMapper.selectCount(new LambdaQueryWrapper<ContestFile>()));
        return overview;
    }

    private <T> List<CchDashboardVo.NameValue> buildNameValueDistribution(
        List<T> records,
        Function<T, String> keyExtractor,
        String defaultValue
    ) {
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (T record : records) {
            String key = keyExtractor.apply(record);
            if (key == null || key.isBlank()) {
                key = defaultValue;
            }
            countMap.merge(key, 1L, Long::sum);
        }
        return countMap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(entry -> {
                CchDashboardVo.NameValue item = new CchDashboardVo.NameValue();
                item.setName(entry.getKey());
                item.setValue(entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }

    private List<CchDashboardVo.TrendItem> buildTrend() {
        List<CchDashboardVo.TrendItem> trend = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            CchDashboardVo.TrendItem item = new CchDashboardVo.TrendItem();
            item.setMonth(month.toString());
            item.setChallengeCount(0L);
            item.setVersionCount(0L);
            item.setProjectCount(0L);
            trend.add(item);
        }

        Map<YearMonth, Long> challengeMap = countByMonth(
            challengeMapper.selectList(new LambdaQueryWrapper<Challenge>().select(Challenge::getCreateTime)),
            Challenge::getCreateTime
        );
        Map<YearMonth, Long> versionMap = countByMonth(
            challengeVersionMapper.selectList(new LambdaQueryWrapper<ChallengeVersion>().select(ChallengeVersion::getCreateTime)),
            ChallengeVersion::getCreateTime
        );
        Map<YearMonth, Long> projectMap = countByMonth(
            projectMapper.selectList(new LambdaQueryWrapper<Project>().select(Project::getCreateTime)),
            Project::getCreateTime
        );

        for (CchDashboardVo.TrendItem item : trend) {
            YearMonth month = YearMonth.parse(item.getMonth());
            item.setChallengeCount(challengeMap.getOrDefault(month, 0L));
            item.setVersionCount(versionMap.getOrDefault(month, 0L));
            item.setProjectCount(projectMap.getOrDefault(month, 0L));
        }
        return trend;
    }

    private <T> Map<YearMonth, Long> countByMonth(List<T> records, Function<T, Date> timeExtractor) {
        Map<YearMonth, Long> map = new HashMap<>();
        for (T record : records) {
            Date date = timeExtractor.apply(record);
            if (date == null) {
                continue;
            }
            YearMonth month = YearMonth.from(date.toInstant().atZone(ZoneId.systemDefault()));
            map.merge(month, 1L, Long::sum);
        }
        return map;
    }

    private List<ProjectVo> buildRecentProjects() {
        LambdaQueryWrapper<Project> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Project::getCreateTime);
        query.last("LIMIT 5");
        return projectMapper.selectVoList(query);
    }
}

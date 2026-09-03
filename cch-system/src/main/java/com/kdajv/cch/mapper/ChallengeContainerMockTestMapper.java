package com.kdajv.cch.mapper;

import com.kdajv.cch.domain.ChallengeContainerMockTest;
import com.kdajv.cch.domain.vo.ChallengeContainerMockTestVo;
import com.kdajv.cch.domain.vo.ContainerMockTestSourceVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 容器模拟测试Mapper接口
 *
 * @author system
 * @date 2026-01-30
 */
public interface ChallengeContainerMockTestMapper extends BaseMapperPlus<ChallengeContainerMockTest, ChallengeContainerMockTestVo> {

    /**
     * 查询草稿列表（属于同一题目的草稿）
     *
     * @param challengeId 题目ID
     * @return 草稿列表
     */
    @Select("""
        SELECT id, id as draftId, challenge_id as challengeId, challenge_name as challengeName,
               create_time as createTime
        FROM t_challenge_draft
        WHERE del_flag = 0
        AND challenge_id = #{challengeId}
        ORDER BY create_time DESC
        """)
    List<ContainerMockTestSourceVo> selectDraftList(@Param("challengeId") Long challengeId);

    /**
     * 查询版本列表（属于指定题目）
     *
     * @param challengeId 题目ID
     * @return 版本列表
     */
    @Select("""
        SELECT cv.id, cv.challenge_id as challengeId, cv.challenge_name as challengeName,
               cv.version_tag as versionTag, cv.draft_id as draftId, cv.create_time as createTime
        FROM t_challenge_version cv
        WHERE cv.del_flag = 0
        AND cv.challenge_id = #{challengeId}
        ORDER BY cv.create_time DESC
        """)
    List<ContainerMockTestSourceVo> selectVersionList(@Param("challengeId") Long challengeId);

    /**
     * 查询活跃的测试列表
     *
     * @return 活跃测试列表
     */
    @Select("""
        SELECT id, draft_id as draftId, source_type as sourceType, source_id as sourceId,
               challenge_name as challengeName, status, error_msg as errorMsg, create_time as createTime,
               expire_time as expireTime, extend_count as extendCount
        FROM t_challenge_container_mock_test
        WHERE del_flag = 0 AND status IN ('running', 'starting', 'failed')
        ORDER BY create_time DESC
        """)
    List<ChallengeContainerMockTestVo> selectActiveTests();

    /**
     * 查询过期的测试ID列表
     *
     * @return 过期的测试ID列表
     */
    @Select("""
        SELECT id FROM t_challenge_container_mock_test
        WHERE del_flag = 0 AND status IN ('running', 'starting', 'failed') AND expire_time < NOW(3)
        """)
    List<Long> selectExpiredTestIds();

}

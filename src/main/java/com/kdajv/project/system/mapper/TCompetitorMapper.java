package com.kdajv.project.system.mapper;

import java.util.List;
import com.kdajv.project.system.domain.TCompetitor;

/**
 * 选手Mapper接口
 * 
 * @author GZY
 * @date 2024-12-18
 */
public interface TCompetitorMapper 
{
    /**
     * 查询选手
     * 
     * @param id 选手主键
     * @return 选手
     */
    public TCompetitor selectTCompetitorById(Long id);

    /**
     * 查询选手列表
     * 
     * @param tCompetitor 选手
     * @return 选手集合
     */
    public List<TCompetitor> selectTCompetitorList(TCompetitor tCompetitor);

    /**
     * 新增选手
     * 
     * @param tCompetitor 选手
     * @return 结果
     */
    public int insertTCompetitor(TCompetitor tCompetitor);

    /**
     * 修改选手
     * 
     * @param tCompetitor 选手
     * @return 结果
     */
    public int updateTCompetitor(TCompetitor tCompetitor);

    /**
     * 删除选手
     * 
     * @param id 选手主键
     * @return 结果
     */
    public int deleteTCompetitorById(Long id);

    /**
     * 批量删除选手
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTCompetitorByIds(Long[] ids);
}

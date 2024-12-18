package com.kdajv.project.system.service.impl;

import java.util.List;

import com.kdajv.common.exception.ServiceException;
import com.kdajv.common.utils.DateUtils;
import com.kdajv.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kdajv.project.system.mapper.TCompetitorMapper;
import com.kdajv.project.system.domain.TCompetitor;
import com.kdajv.project.system.service.ITCompetitorService;

/**
 * 选手Service业务层处理
 * 
 * @author GZY
 * @date 2024-12-18
 */
@Service
public class TCompetitorServiceImpl implements ITCompetitorService 
{
    private static final Logger log = LoggerFactory.getLogger(TCompetitorServiceImpl.class);
    @Autowired
    private TCompetitorMapper tCompetitorMapper;

    /**
     * 查询选手
     * 
     * @param id 选手主键
     * @return 选手
     */
    @Override
    public TCompetitor selectTCompetitorById(Long id)
    {
        return tCompetitorMapper.selectTCompetitorById(id);
    }

    /**
     * 查询选手列表
     * 
     * @param tCompetitor 选手
     * @return 选手
     */
    @Override
    public List<TCompetitor> selectTCompetitorList(TCompetitor tCompetitor)
    {
        return tCompetitorMapper.selectTCompetitorList(tCompetitor);
    }

    /**
     * 新增选手
     * 
     * @param tCompetitor 选手
     * @return 结果
     */
    @Override
    public int insertTCompetitor(TCompetitor tCompetitor)
    {
        tCompetitor.setCreateTime(DateUtils.getNowDate());
        return tCompetitorMapper.insertTCompetitor(tCompetitor);
    }

    /**
     * 修改选手
     * 
     * @param tCompetitor 选手
     * @return 结果
     */
    @Override
    public int updateTCompetitor(TCompetitor tCompetitor)
    {
        tCompetitor.setUpdateTime(DateUtils.getNowDate());
        return tCompetitorMapper.updateTCompetitor(tCompetitor);
    }

    /**
     * 批量删除选手
     * 
     * @param ids 需要删除的选手主键
     * @return 结果
     */
    @Override
    public int deleteTCompetitorByIds(Long[] ids)
    {
        return tCompetitorMapper.deleteTCompetitorByIds(ids);
    }

    /**
     * 删除选手信息
     * 
     * @param id 选手主键
     * @return 结果
     */
    @Override
    public int deleteTCompetitorById(Long id)
    {
        return tCompetitorMapper.deleteTCompetitorById(id);
    }

    /**
     * 导入选手数据
     *
     * @param competitorList 选手数据列表
     * @param operName       操作用户
     * @return 结果
     */
    @Override
    public String importUser(List<TCompetitor> competitorList, String operName) {
        if (StringUtils.isNull(competitorList) || competitorList.isEmpty())
        {
            throw new ServiceException("导入选手数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (TCompetitor competitor : competitorList)
        {
            try
            {
                competitor.setCreateBy(operName);
                int i = tCompetitorMapper.insertTCompetitor(competitor);
                successNum++;
                successMsg.append("<br/>").append(successNum).append("、选手 ").append(competitor.getName()).append(" 导入成功");
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、账号 " + competitor.getName() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }
}

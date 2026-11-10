package com.yunfan.data.service;


import com.yunfan.data.model.dto.Top10DataSetDTO;
import com.yunfan.data.model.vo.Top10DataVO;

/**
 * @author xleyyds1314
 * @since 2025-5-18 19:06:26
 **/
public interface Top10Service {

    /**
     * 获取top数据
     *
     * @return
     */
    Top10DataVO getTop10Data();

    /**
     * top 10数据设置
     * @param top10DataSetDTO
     */
    void setTop10Data(Top10DataSetDTO top10DataSetDTO);
}
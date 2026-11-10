package com.yunfan.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.common.domain.query.PageQuery;
import com.yunfan.learning.domain.dto.PointsExchangeRecordDTO;
import com.yunfan.learning.domain.po.PointsExchangeRecords;
import com.yunfan.learning.domain.vo.PointsExchangeRecordsVO;

public interface IPointsExchangeRecordsService extends IService<PointsExchangeRecords> {

    void exchangeItem(PointsExchangeRecordDTO dto);

    void updateExchangeStatus(Long id, Byte status);

    PageDTO<PointsExchangeRecordsVO> queryExchangeRecordsByUser(Long userId, PageQuery query);

    PageDTO<PointsExchangeRecords> queryAllExchangeRecords(PageQuery query, Long itemId, Byte status);

    PointsExchangeRecordsVO queryExchangeRecordById(Long id);

    void cancelExchangeStatus(Long id);
}
package com.yunfan.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.pay.domain.po.PayChannel;
import com.yunfan.pay.domain.po.ReconciliationRecord;
import com.yunfan.pay.domain.query.ReconciliationQueryDTO;

public interface IReconciliationService extends IService<ReconciliationRecord> {
    /**
     * 根据状态分页查询对账列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageDTO<ReconciliationRecord> queryReconciliationList(ReconciliationQueryDTO queryDTO);


    void reconcilePayOrders();

    void reconcileRefundOrders();
}
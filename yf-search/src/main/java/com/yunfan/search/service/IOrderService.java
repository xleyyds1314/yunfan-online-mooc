package com.yunfan.search.service;

import com.yunfan.api.dto.trade.OrderAnalysisDTO;
import com.yunfan.search.domain.po.Order;

import java.util.List;

/**
 * @author xleyyds1314
 * @date 2025/5/25 9:48
 */
public interface IOrderService {

    void saveAll(List<Order> orders);
}

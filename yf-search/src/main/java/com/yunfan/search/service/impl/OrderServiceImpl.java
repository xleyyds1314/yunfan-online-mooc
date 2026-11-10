package com.yunfan.search.service.impl;/**
 * @author xleyyds1314
 * @date 2025/5/25 9:48
 */

import com.yunfan.api.dto.trade.OrderAnalysisDTO;
import com.yunfan.search.domain.po.Order;
import com.yunfan.search.repository.OrderRepository;
import com.yunfan.search.service.IOrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: fsq
 * @Date: 2025/5/25 9:48
 * @Version: 1.0
 */
@Service
public class OrderServiceImpl implements IOrderService {

    @Resource
    private OrderRepository  orderRepository;

    @Override
    public void saveAll(List<Order> orders) {
        orderRepository.saveAll(orders);
    }

}

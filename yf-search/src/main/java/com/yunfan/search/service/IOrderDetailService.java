package com.yunfan.search.service;

import com.yunfan.search.domain.po.OrderDetail;

import java.util.List;

/**
 * @author xleyyds1314
 * @date 2025/5/25 9:48
 */
public interface IOrderDetailService {


    void saveAll(List<OrderDetail> orderDetails);
}

package com.yunfan.trade.mapper;

import com.yunfan.trade.domain.po.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 订单 Mapper 接口
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-08-29
 */
public interface OrderMapper extends BaseMapper<Order> {

    Order getById(Long id);
}

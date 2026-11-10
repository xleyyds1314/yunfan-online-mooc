package com.yunfan.trade.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.pay.sdk.dto.PayResultDTO;
import com.yunfan.trade.constants.OrderCancelReason;
import com.yunfan.trade.domain.dto.PlaceOrderDTO;
import com.yunfan.trade.domain.po.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.trade.domain.po.OrderDetail;
import com.yunfan.trade.domain.query.OrderPageQuery;
import com.yunfan.trade.domain.vo.OrderConfirmVO;
import com.yunfan.trade.domain.vo.OrderPageVO;
import com.yunfan.trade.domain.vo.OrderVO;
import com.yunfan.trade.domain.vo.PlaceOrderResultVO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-08-29
 */
public interface IOrderService extends IService<Order> {

    PlaceOrderResultVO placeOrder(PlaceOrderDTO placeOrderDTO);

    @Transactional
    void saveOrderAndDetails(Order order, List<OrderDetail> orderDetails);

    void cancelOrder(Long orderId, OrderCancelReason cancelReason);

    void deleteOrder(Long id);

    PageDTO<OrderPageVO> queryMyOrderPage(OrderPageQuery pageQuery);

    OrderVO queryOrderById(Long id);

    PlaceOrderResultVO queryOrderStatus(Long orderId);

    List<Order> queryOrderBetweenTime(LocalDateTime date1, LocalDateTime date2);

    void handlePaySuccess(PayResultDTO payResult);

    PlaceOrderResultVO enrolledFreeCourse(Long courseId);

    OrderConfirmVO prePlaceOrder(List<Long> courseIds);

}

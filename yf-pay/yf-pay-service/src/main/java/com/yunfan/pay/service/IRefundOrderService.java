package com.yunfan.pay.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.pay.domain.po.RefundOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.pay.sdk.dto.RefundApplyDTO;
import com.yunfan.pay.sdk.dto.RefundResultDTO;

/**
 * <p>
 * 退款订单 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-08-26
 */
public interface IRefundOrderService extends IService<RefundOrder> {

    RefundResultDTO applyRefund(RefundApplyDTO refundApplyDTO);

    RefundResultDTO queryRefundResult(Long bizRefundOrderId);

    RefundOrder queryByRefundOrderNo(Long refundOrderNo);

    PageDTO<RefundOrder> queryRefundingOrderByPage(int pageNo, int size);

    void checkRefundOrder(RefundOrder refundOrder);

    RefundResultDTO queryRefundDetail(Long bizRefundOrderId);
}

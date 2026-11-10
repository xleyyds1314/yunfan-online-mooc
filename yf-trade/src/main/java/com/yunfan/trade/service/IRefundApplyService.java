package com.yunfan.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.pay.sdk.dto.RefundResultDTO;
import com.yunfan.trade.domain.dto.ApproveFormDTO;
import com.yunfan.trade.domain.dto.RefundCancelDTO;
import com.yunfan.trade.domain.dto.RefundFormDTO;
import com.yunfan.trade.domain.po.RefundApply;
import com.yunfan.trade.domain.query.RefundApplyPageQuery;
import com.yunfan.trade.domain.vo.RefundApplyPageVO;
import com.yunfan.trade.domain.vo.RefundApplyVO;

import java.util.List;

/**
 * <p>
 * 退款申请 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-08-29
 */
public interface IRefundApplyService extends IService<RefundApply> {

    List<RefundApply> queryByDetailId(Long id);

    void applyRefund(RefundFormDTO refundFormDTO);

    PageDTO<RefundApplyPageVO> queryRefundApplyByPage(RefundApplyPageQuery pageQuery);

    RefundApplyVO queryRefundDetailById(Long id);

    RefundApplyVO nextRefundApplyToApprove();

    void approveRefundApply(ApproveFormDTO approveDTO);

    void cancelRefundApply(RefundCancelDTO cancelDTO);

    RefundApplyVO queryRefundDetailByDetailId(Long id);

    void handleRefundResult(RefundResultDTO refundResult);

    List<RefundApply> queryApplyToSend(int page, int size);

    void sendRefundRequest(RefundApply refundApply);

    boolean checkRefundStatus(RefundApply refundApply);

    void againApplyRefund(Long id);
}

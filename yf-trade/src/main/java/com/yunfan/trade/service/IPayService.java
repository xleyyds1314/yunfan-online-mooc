package com.yunfan.trade.service;

import com.yunfan.trade.domain.dto.OrderDelayQueryDTO;
import com.yunfan.trade.domain.dto.PayApplyFormDTO;
import com.yunfan.trade.domain.vo.PayChannelVO;

import java.util.List;

public interface IPayService {
    List<PayChannelVO> queryPayChannels();

    String applyPayOrder(PayApplyFormDTO payApply);

    void queryPayResult(OrderDelayQueryDTO message);
}

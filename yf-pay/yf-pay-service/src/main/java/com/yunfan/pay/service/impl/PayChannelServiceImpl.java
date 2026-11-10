package com.yunfan.pay.service.impl;

import com.yunfan.common.utils.BeanUtils;
import com.yunfan.common.utils.UserContext;
import com.yunfan.pay.sdk.dto.PayChannelDTO;
import com.yunfan.pay.domain.po.PayChannel;
import com.yunfan.pay.mapper.PayChannelMapper;
import com.yunfan.pay.service.IPayChannelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付渠道 服务实现类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-08-26
 */
@Service
public class PayChannelServiceImpl extends ServiceImpl<PayChannelMapper, PayChannel> implements IPayChannelService {

    @Override
    public Long addPayChannel(PayChannelDTO channelDTO) {
        // 1.属性转换
        PayChannel payChannel = BeanUtils.toBean(channelDTO, PayChannel.class);
        Long userId = UserContext.getUser();
        payChannel.setCreater(userId);
        payChannel.setUpdater(userId);
        // 2.保存
        save(payChannel);
        return payChannel.getId();
    }

    @Override
    public void updatePayChannel(PayChannelDTO channelDTO) {
        // 1.属性转换
        PayChannel payChannel = BeanUtils.toBean(channelDTO, PayChannel.class);
        payChannel.setUpdater(UserContext.getUser());
        // 2.保存
        updateById(payChannel);
    }

    @Override
    public PayChannelDTO getPayChannelById(Long id) {
        PayChannel channel = getById(id);
        return  BeanUtils.toBean(channel, PayChannelDTO.class);
    }
}

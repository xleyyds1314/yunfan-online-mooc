package com.yunfan.message.api.client;

import com.yunfan.common.autoconfigure.mq.RabbitMqHelper;
import com.yunfan.common.constants.MqConstants;
import com.yunfan.message.domain.dto.SmsInfoDTO;

public class AsyncSmsClient {
    private final RabbitMqHelper mqHelper;

    public AsyncSmsClient(RabbitMqHelper mqHelper) {
        this.mqHelper = mqHelper;
    }

    /**
     * 基于 MQ 异步发送短信
     * @param smsInfoDTO 短信相关信息
     */
    public void sendMessage(SmsInfoDTO smsInfoDTO){
        mqHelper.send(MqConstants.Exchange.SMS_EXCHANGE, MqConstants.Key.SMS_MESSAGE, smsInfoDTO);
    }
}

package com.yunfan.message.thirdparty;

import com.yunfan.api.dto.sms.SmsInfoDTO;
import com.yunfan.message.domain.po.MessageTemplate;

/**
 * 第三方接口对接平台
 */
public interface ISmsHandler {

    /**
     * 发送短信
     */
    void send(SmsInfoDTO platformSmsInfoDTO, MessageTemplate template);


}

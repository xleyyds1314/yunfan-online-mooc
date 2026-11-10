package com.yunfan.message.service;

import com.yunfan.api.dto.sms.SmsInfoDTO;
import com.yunfan.api.dto.user.UserDTO;
import com.yunfan.message.domain.po.NoticeTemplate;

import java.util.List;

public interface ISmsService {
    void sendMessageByTemplate(NoticeTemplate noticeTemplate, List<UserDTO> users);

    void sendMessage(SmsInfoDTO smsInfoDTO);

    void sendMessageAsync(SmsInfoDTO smsInfoDTO);
}

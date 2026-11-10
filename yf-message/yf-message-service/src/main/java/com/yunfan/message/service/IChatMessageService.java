package com.yunfan.message.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.message.domain.dto.ChatMessageDTO;
import com.yunfan.message.domain.po.ChatMessage;
import com.yunfan.message.domain.query.ChatHistoryQuery;
import com.yunfan.message.domain.vo.ChatMessageVO;

import java.util.List;

public interface IChatMessageService {
    Long sendMessage(ChatMessageDTO messageDTO);
    PageDTO<ChatMessageVO> getHistoryMessages(ChatHistoryQuery query);
    void markMessagesAsRead(List<Long> messageIds);
}
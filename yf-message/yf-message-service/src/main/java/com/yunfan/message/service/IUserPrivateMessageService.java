package com.yunfan.message.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.message.domain.dto.UserPrivateMessageFormDTO;
import com.yunfan.message.domain.po.UserPrivateMessage;
import com.yunfan.message.domain.query.UserPrivateMessageQuery;
import com.yunfan.message.domain.vo.UserPrivateMessageVO;

/**
 * @author xleyyds1314
 * @date 2025/5/21 19:03
 */
public interface IUserPrivateMessageService {
    Boolean sendMessage(UserPrivateMessageFormDTO userPrivateMessageFormDTO);

    PageDTO<UserPrivateMessageVO> getMessageHistory(UserPrivateMessageQuery query);

    //根据用户id得到与其最后一条消息
    UserPrivateMessage getLastMessage(Long otherUserId);

    //根据对方用户id得到未读数
    Integer getUnreadCount(Long otherUserId);

    //得到自己总的私信未读数
    Integer getAllUnreadCountBySelf(Long userId);
}

package com.yunfan.message.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.message.domain.dto.ChatGroupDTO;
import com.yunfan.message.domain.dto.ChatMessageDTO;
import com.yunfan.message.domain.dto.GroupMemberDTO;
import com.yunfan.message.domain.po.ChatGroup;
import com.yunfan.message.domain.po.ChatMessage;
import com.yunfan.message.domain.query.ChatGroupQuery;
import com.yunfan.message.domain.vo.ChatGroupVO;

import java.util.List;

public interface IChatGroupService {
    Long createGroup(ChatGroupDTO groupDTO);
    List<ChatGroupVO> getUserGroups(ChatGroupQuery query,Long userId);
    void addGroupMember(Long groupId, GroupMemberDTO memberDTO);
    void removeGroupMember(Long groupId, Long userId);
    PageDTO<ChatGroupVO> getAllGroups(ChatGroupQuery query);

    void addToGroup(Long groupId, Long userId);
}
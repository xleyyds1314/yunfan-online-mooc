package com.yunfan.message.service;

import com.yunfan.message.domain.dto.UserInboxDTO;
import com.yunfan.api.dto.user.UserDTO;
import com.yunfan.message.domain.query.UserInboxQuery;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.message.domain.po.NoticeTemplate;
import com.yunfan.message.domain.po.UserInbox;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户通知记录 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-08-19
 */
public interface IUserInboxService extends IService<UserInbox> {

    void saveNoticeToInbox(NoticeTemplate noticeTemplate, List<UserDTO> users);

    PageDTO<UserInboxDTO> queryUserInBoxesPage(UserInboxQuery query);

    Integer getUnReadCountByType(Integer type);

    Integer getUnReadCount();

    Boolean markMessageAsRead(Long id);

    Boolean markAllMessagesAsRead();
}

package com.yunfan.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunfan.message.domain.po.UserInbox;

/**
 * <p>
 * 用户通知记录 Mapper 接口
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-08-20
 */
public interface UserInboxMapper extends BaseMapper<UserInbox> {

    UserInbox queryLatestPublicNotice(Long userId);
}

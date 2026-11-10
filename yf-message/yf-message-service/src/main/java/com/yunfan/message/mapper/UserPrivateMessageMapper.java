package com.yunfan.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunfan.message.domain.po.SmsThirdPlatform;
import com.yunfan.message.domain.po.UserConversation;
import com.yunfan.message.domain.po.UserPrivateMessage;
import com.yunfan.message.domain.query.UserConversationQuery;
import com.yunfan.message.domain.query.UserPrivateMessageQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 第三方云通讯平台 Mapper 接口
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-08-20
 */
public interface UserPrivateMessageMapper extends BaseMapper<UserPrivateMessage> {

    IPage<UserPrivateMessage> getMessageHistory(
            @Param("page") Page<?> page,
            @Param("query") UserPrivateMessageQuery query,
            @Param("userId") Long userId
    );
}

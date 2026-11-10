package com.yunfan.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunfan.message.domain.po.ChatGroup;
import com.yunfan.message.domain.po.SmsThirdPlatform;
import com.yunfan.message.domain.po.UserConversation;
import com.yunfan.message.domain.query.ChatGroupQuery;
import com.yunfan.message.domain.query.UserConversationQuery;
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
public interface ChatGroupMapper extends BaseMapper<ChatGroup> {
    IPage<ChatGroup> selectUserGroups(
            @Param("page") Page<?> page,
            @Param("userId") Long userId,
            @Param("query") ChatGroupQuery query
    );
}

package com.yunfan.message.domain.query;

import com.yunfan.common.domain.query.PageQuery;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "用户收件箱查询对象")
@Data
public class UserInboxQuery extends PageQuery {
    private Integer type;
    private Boolean isRead;
}

package com.yunfan.user.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.user.domain.query.UserPageQuery;
import com.yunfan.user.domain.vo.StaffVO;

/**
 * <p>
 * 员工详情表 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-07-12
 */
public interface IStaffService {
    PageDTO<StaffVO> queryStaffPage(UserPageQuery pageQuery);
}

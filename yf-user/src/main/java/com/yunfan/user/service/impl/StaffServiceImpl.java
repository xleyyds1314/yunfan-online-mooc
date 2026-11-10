package com.yunfan.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunfan.api.cache.RoleCache;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.common.enums.UserType;
import com.yunfan.common.utils.BeanUtils;
import com.yunfan.user.domain.po.UserDetail;
import com.yunfan.user.domain.query.UserPageQuery;
import com.yunfan.user.domain.vo.StaffVO;
import com.yunfan.user.service.IStaffService;
import com.yunfan.user.service.IUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 员工详情表 服务实现类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-07-12
 */
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements IStaffService {

    private final IUserDetailService detailService;
    private final RoleCache roleCache;
    @Override
    public PageDTO<StaffVO> queryStaffPage(UserPageQuery query) {
        // 1.搜索
        Page<UserDetail> p = detailService.queryUserDetailByPage(query, UserType.STAFF);
        // 2.处理vo
        return PageDTO.of(p, u -> {
            StaffVO v = BeanUtils.toBean(u, StaffVO.class);
            v.setRoleName(roleCache.getRoleName(u.getRoleId()));
            return v;
        });
    }
}

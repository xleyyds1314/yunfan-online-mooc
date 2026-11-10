package com.yunfan.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.auth.domain.po.RolePrivilege;

import java.util.List;

/**
 * <p>
 * 账户、角色关联表 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-06-16
 */
public interface IRolePrivilegeService extends IService<RolePrivilege> {

    void removeByPrivilegeId(Long id);

    void removeByRoleId(Long id);

    void deleteRolePrivileges(Long roleId, List<Long> privilegeIds);
}

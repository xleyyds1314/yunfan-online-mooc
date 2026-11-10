package com.yunfan.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.api.dto.user.LoginFormDTO;
import com.yunfan.api.dto.user.UserDTO;
import com.yunfan.common.domain.dto.LoginUserDTO;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.user.domain.dto.StudentFormDTO;
import com.yunfan.user.domain.dto.UserFormDTO;
import com.yunfan.user.domain.po.User;
import com.yunfan.user.domain.query.UserPageQuery;
import com.yunfan.user.domain.query.UserSimplePageQuery;
import com.yunfan.user.domain.vo.UserDetailVO;
import com.yunfan.user.domain.vo.UserPageVO;

import java.util.List;

/**
 * <p>
 * 学员用户表 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-06-28
 */
public interface IUserService extends IService<User> {
    LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isStaff);

    void resetPassword(Long userId);

    UserDetailVO myInfo();

    void addUserByPhone(User user, String code);

    void updatePasswordByPhone(String cellPhone, String code, String password);

    Long saveUser(UserDTO userDTO);

    void updateUser(UserDTO userDTO);

    void updateUserWithPassword(UserFormDTO userDTO);

    Boolean checkCellPhone(String cellPhone);

    PageDTO<UserPageVO> queryUserPage(UserSimplePageQuery pageQuery);

    Boolean checkPasswd(String password);
}

package com.yunfan.user.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.user.domain.dto.StudentFormDTO;
import com.yunfan.user.domain.dto.StudentUpdateDTO;
import com.yunfan.user.domain.dto.StudentUpdatePasswordDTO;
import com.yunfan.user.domain.query.UserPageQuery;
import com.yunfan.user.domain.vo.StudentPageVO;

/**
 * <p>
 * 学员详情表 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-07-12
 */
public interface IStudentService {

    void saveStudent(StudentFormDTO studentFormDTO);

    void updateMyPassword(StudentFormDTO studentFormDTO);

    PageDTO<StudentPageVO> queryStudentPage(UserPageQuery pageQuery);

    void updateStudent(StudentUpdateDTO studentUpdateDTO);

    void updateBindPhone(String cellPhone, String code);

    void updatePassword(StudentUpdatePasswordDTO dto);
}

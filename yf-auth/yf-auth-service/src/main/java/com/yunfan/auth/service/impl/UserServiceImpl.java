package com.yunfan.auth.service.impl;/**
 * @author xleyyds1314
 * @date 2025/5/20 13:41
 */

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.api.client.user.StudentClient;
import com.yunfan.api.client.user.UserClient;
import com.yunfan.api.dto.user.StudentFormDTO;
import com.yunfan.auth.service.ICodeService;
import com.yunfan.auth.service.IUserService;
import com.yunfan.common.exceptions.BizIllegalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: fsq
 * @Date: 2025/5/20 13:41
 * @Version: 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl  implements IUserService {

    private final ICodeService codeService;
    private final StudentClient studentClient;
    private final UserClient userClient;

    @Override
    public void register(StudentFormDTO dto) {
//        codeService.verifyCode(dto.getCellPhone(), dto.getCode());
        //用户微服务都校验了
        studentClient.registerStudent(dto);
    }

    @Override
    public void updateMyPassword(StudentFormDTO dto) {
        Long id = userClient.exchangeUserIdWithPhone(dto.getCellPhone());
        if(id==null){
            throw new BizIllegalException("手机号未注册");
        }
//        codeService.verifyCode(dto.getCellPhone(), dto.getCode());
        studentClient.updateMyPassword(dto);
    }
}

package com.yunfan.auth.service;

import com.yunfan.common.exceptions.BadRequestException;

/**
 * @author xleyyds1314
 * @date 2025/5/20 10:05
 */
public interface ICodeService {

    void sendVerifyCode(String cellPhone);

    void verifyCode(String phone, String code) ;
}

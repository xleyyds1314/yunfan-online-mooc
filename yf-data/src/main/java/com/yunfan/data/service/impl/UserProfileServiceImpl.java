package com.yunfan.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunfan.data.mapper.CourseProfileMapper;
import com.yunfan.data.mapper.UserProfileMapper;
import com.yunfan.data.model.po.CourseProfile;
import com.yunfan.data.model.po.UserProfile;
import com.yunfan.data.service.ICourseProfileService;
import com.yunfan.data.service.IUserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description：用户画像服务实现类
 */
@Slf4j
@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements IUserProfileService {

}

package com.yunfan.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunfan.data.mapper.CourseProfileMapper;
import com.yunfan.data.mapper.DpvMapper;
import com.yunfan.data.model.po.CourseProfile;
import com.yunfan.data.model.po.Dpv;
import com.yunfan.data.service.ICourseProfileService;
import com.yunfan.data.service.IDpvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description：课程画像服务实现类
 */
@Slf4j
@Service
public class CourseProfileServiceImpl extends ServiceImpl<CourseProfileMapper, CourseProfile> implements ICourseProfileService {

}

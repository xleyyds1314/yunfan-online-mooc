package com.yunfan.learning.service.impl;/**
 * @author xleyyds1314
 * @date 2025/5/22 9:05
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunfan.api.client.course.CourseClient;
import com.yunfan.api.constants.CourseStatus;
import com.yunfan.api.dto.course.CourseSearchDTO;
import com.yunfan.api.dto.course.CourseSimpleInfoDTO;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.common.exceptions.BadRequestException;
import com.yunfan.common.utils.BeanUtils;
import com.yunfan.common.utils.CollUtils;
import com.yunfan.common.utils.UserContext;
import com.yunfan.learning.domain.dto.CollectFormDTO;
import com.yunfan.learning.domain.po.LearningLesson;
import com.yunfan.learning.domain.po.LessonCollect;
import com.yunfan.learning.domain.query.LessonCollectQuery;
import com.yunfan.learning.domain.vo.LearningLessonVO;
import com.yunfan.learning.domain.vo.LessonCollectVO;
import com.yunfan.learning.enums.LessonStatus;
import com.yunfan.learning.mapper.LearningLessonMapper;
import com.yunfan.learning.mapper.LessonCollectMapper;
import com.yunfan.learning.service.ILearningLessonService;
import com.yunfan.learning.service.ILessonCollectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: fsq
 * @Date: 2025/5/22 9:05
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LessonCollectServiceImpl extends ServiceImpl<LessonCollectMapper, LessonCollect> implements ILessonCollectService {


    final ILearningLessonService learningLessonService;
    final CourseClient courseClient;


    @Override
    public PageDTO<LessonCollectVO> queryMyCollects(LessonCollectQuery query) {
        //获取当前登录人
        Long userId = UserContext.getUser();
        //分页查询我的课表
        Page<LessonCollect> page = this.lambdaQuery()
                .eq(LessonCollect::getUserId, userId)
                .page(query.toMpPage("create_time",false));
        List<LessonCollect> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        //远程调用课程服务，给vo的课程数，封面，章节数赋值
        Set<Long> courseIds = records.stream().map(LessonCollect::getCourseId).collect(Collectors.toSet());
        List<CourseSimpleInfoDTO> cinfos = courseClient.getSimpleInfoList(courseIds);
        if(CollUtils.isEmpty(cinfos)){
            throw new BadRequestException("课程不存在");
        }
        //将cinfos课程集合转换为map结构<课程id,对象>
        Map<Long, CourseSimpleInfoDTO> infoDTOMap = cinfos.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));

        List<LessonCollectVO> voList=new ArrayList<>();
        //将po中的数据封装到vo中
        for (LessonCollect record : records) {
            LessonCollectVO vo = BeanUtils.copyBean(record, LessonCollectVO.class);
            CourseSimpleInfoDTO infoDTO = infoDTOMap.get(record.getCourseId());
            if(infoDTO!=null) {
                vo.setCourseName(infoDTO.getName());
                vo.setCourseCoverUrl(infoDTO.getCoverUrl());
                vo.setSections(infoDTO.getSectionNum());
            }
            voList.add(vo);
        }
        //返回
        return PageDTO.of(page,voList);
    }

    @Override
    public void addCollect(CollectFormDTO dto) {
        //获取当前登录人
        Long userId = UserContext.getUser();

        CourseSearchDTO searchInfo = courseClient.getSearchInfo(dto.getCourseId());
        if(searchInfo==null){
            throw new BadRequestException("不存在的课程id");
        }

        LambdaQueryWrapper<LessonCollect> lessonCollectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        lessonCollectLambdaQueryWrapper.eq(LessonCollect::getUserId, userId)
                .eq(LessonCollect::getCourseId, dto.getCourseId());
        LessonCollect lessonCollect = baseMapper.selectOne(lessonCollectLambdaQueryWrapper);
        if(dto.getCollected()){
            //收藏
            if(lessonCollect!=null){
                throw new BadRequestException("该课程已收藏");
            }
            LessonCollect collect = new LessonCollect();
            collect.setUserId(userId);
            collect.setCourseId(dto.getCourseId());
            save(collect);
        }else{
            if(lessonCollect==null){
                throw new BadRequestException("该课程未收藏");
            }
            //取消收藏
            this.baseMapper.deleteById(lessonCollect);
        }
    }

    @Override
    public Boolean isCollected(Long lessonId) {
        Long userId = UserContext.getUser();
        LambdaQueryWrapper<LessonCollect> lessonCollectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        lessonCollectLambdaQueryWrapper.eq(LessonCollect::getUserId, userId)
                .eq(LessonCollect::getCourseId, lessonId);
        Integer count = baseMapper.selectCount(lessonCollectLambdaQueryWrapper);

        //true  表示已收藏 false表示未收藏
        return count>0;
    }
}

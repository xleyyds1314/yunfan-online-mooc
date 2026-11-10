package com.yunfan.data.service.impl;

import com.yunfan.api.dto.data.CourseDataVO;
import com.yunfan.api.dto.data.OrderDataVO;
import com.yunfan.common.utils.JsonUtils;
import com.yunfan.common.utils.NumberUtils;
import com.yunfan.data.constants.DataTypeEnum;
import com.yunfan.data.constants.RedisConstants;
import com.yunfan.data.model.dto.BoardDataSetDTO;
import com.yunfan.data.model.vo.*;
import com.yunfan.data.service.BoardService;
import com.yunfan.data.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.yunfan.data.constants.RedisConstants.KEY_BOARD_DATA;

/**
 * @ClassName BoardServiceImpl
 * @Author xleyyds1314
 * @Date 2022/10/10 16:32
 * @Version
 **/
@Service
@Slf4j
public class BoardServiceImpl implements BoardService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public EchartsVO boardData(List<Integer> types) {
        // 1.定义echarts变量
        EchartsVO echartsVO = new EchartsVO();
        List<AxisVO> yAxis = new ArrayList<>();
        List<SerierVO> series = new ArrayList<>();
        // 2.遍历数据类型types
        // 2.1.数据版本
        int version = DataUtils.getVersion(1);

        for (Integer type : types) {
            // 2.1.获取数据类型
            DataTypeEnum dataTypeEnum = DataTypeEnum.get(type);
            // 2.2.获取数据
            Object originData = redisTemplate.opsForHash().get(KEY_BOARD_DATA + version, type.toString());
            List<Double> data = originData == null
                    ? new ArrayList<>()
                    : JsonUtils.toList(originData.toString(), Double.class);
            // 2.3.计算最大最小值
            Double max = NumberUtils.null2Zero(NumberUtils.max(data));
            Double min = NumberUtils.null2Zero(NumberUtils.min(data));
            // 2.2.设置数据
            series.add(new SerierVO(
                    dataTypeEnum.nameWithUnit(),
                    dataTypeEnum.getAxisType(),
                    data,
                    max + dataTypeEnum.getUnit(),
                    min + dataTypeEnum.getUnit()
                    ));
            // 2.3.设置y轴数据
            yAxis.add(AxisVO.builder()
                    .max(max)
                    .min(NumberUtils.setScale(min * 0.9))
                    .interval(((int)NumberUtils.div((max - min * 0.9), 10.0) + 1) * 1.0)
                    .average(
                            NumberUtils.setScale(NumberUtils.null2Zero(NumberUtils.average(data))))
                    .type(AxisVO.TYPE_VALUE)
                    .build());
        }
        // 3.封装数据
        // 3.1.x轴数据
        echartsVO.setXAxis(Collections.singletonList(AxisVO.last15Day()));
        // 3.2.y轴数据
        echartsVO.setYAxis(yAxis);
        // 3.3.series数据
        echartsVO.setSeries(series);
        return echartsVO;
    }

    @Override
    public void setBoardData(BoardDataSetDTO boardDataSetDTO) {
        String key = KEY_BOARD_DATA + boardDataSetDTO.getVersion();
        redisTemplate.opsForHash()
                .put(key,
                        boardDataSetDTO.getType().toString(),
                        JsonUtils.toJsonStr(boardDataSetDTO.getData()));
    }


    @Override
    public CourseDataVO courseBoardData() {
        // 1.数据redis存储key
        String key = RedisConstants.KEY_COURSE_BOARD + DataUtils.getVersion(1);
        // 2.获取数据
        Object originData = redisTemplate.opsForValue().get(key);
        // 2.1.数据判空
        if (originData == null) {
            return new CourseDataVO();
        }
        return JsonUtils.toBean(originData.toString(), CourseDataVO.class);
    }

    @Override
    public OrderDataVO orderBoardData() {
        // 1.数据redis存储key
        String key = RedisConstants.KEY_ORDER_BOARD + DataUtils.getVersion(1);
        // 2.获取数据
        Object originData = redisTemplate.opsForValue().get(key);
        // 2.1.数据判空
        if (originData == null) {
            return new OrderDataVO();
        }
        return JsonUtils.toBean(originData.toString(), OrderDataVO.class);
    }

    @Override
    public void updateOrderData(OrderDataVO orderData) {
        // 1. 构建 Redis 存储 key（与查询逻辑保持一致）
        String key = RedisConstants.KEY_ORDER_BOARD + DataUtils.getVersion(1);

        try {
            // 2. 将对象序列化为 JSON 字符串
            String jsonData = JsonUtils.toJsonStr(orderData);

            // 3. 存储到 Redis 中（设置合理的过期时间，例如 1 天）
            redisTemplate.opsForValue().set(key, jsonData, 1, TimeUnit.DAYS);

            log.info("订单数据已更新到 Redis，key: {}", key);
        } catch (Exception e) {
            log.error("更新订单数据到 Redis 失败", e);
            // 可以添加降级处理或重试逻辑
        }
    }

    @Override
    public void updateCourseData(CourseDataVO courseData) {
        // 1. 构建 Redis 存储 key（与查询逻辑保持一致）
        String key = RedisConstants.KEY_COURSE_BOARD + DataUtils.getVersion(1);
        try {
            // 2. 将对象序列化为 JSON 字符串
            String jsonData = JsonUtils.toJsonStr(courseData);

            // 3. 存储到 Redis 中（设置合理的过期时间，例如 1 天）
            redisTemplate.opsForValue().set(key, jsonData, 1, TimeUnit.DAYS);

            log.info("课程数据已更新到 Redis，key: {}", key);
        } catch (Exception e) {
            log.error("更新课程数据到 Redis 失败", e);
            // 可以添加降级处理或重试逻辑
        }
    }
}

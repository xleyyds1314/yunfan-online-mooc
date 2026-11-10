package com.yunfan.learning.service.impl;

import com.yunfan.common.utils.DateUtils;
import com.yunfan.learning.domain.po.PointsBoardSeason;
import com.yunfan.learning.domain.vo.PointsBoardSeasonVO;
import com.yunfan.learning.mapper.PointsBoardSeasonMapper;
import com.yunfan.learning.service.IPointsBoardSeasonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.yunfan.learning.constants.LearningConstants.POINTS_BOARD_TABLE_PREFIX;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xleyyds1314
 * @since 2023-10-26
 */
@Service
public class PointsBoardSeasonServiceImpl extends ServiceImpl<PointsBoardSeasonMapper, PointsBoardSeason> implements IPointsBoardSeasonService {

    @Override
    public void createPointsBoardLatestTable(Integer id) {
        getBaseMapper().createPointsBoardTable(POINTS_BOARD_TABLE_PREFIX + id);
    }

}


package com.yunfan.remark.service;

import com.yunfan.remark.domain.dto.LikeRecordFormDTO;
import com.yunfan.remark.domain.po.LikedRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 点赞记录表 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2023-10-25
 */
public interface ILikedRecordService extends IService<LikedRecord> {

    void addLikeRecord(LikeRecordFormDTO dto);

    Set<Long> getLikesStatusByBizIds(List<Long> bizIds);

    void readLikedTimesAndSendMessage(String bizType, int maxBizSize);
}

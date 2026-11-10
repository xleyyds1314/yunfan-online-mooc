package com.yunfan.learning.service;

import com.yunfan.learning.domain.dto.ShareDetailDTO;
import com.yunfan.learning.domain.dto.ShareLinkDTO;

/**
 * @author xleyyds1314
 * @date 2025/6/15 14:39
 */
public interface IShareService {
    ShareLinkDTO generateShareLink(Long userId, Long courseId);

    ShareDetailDTO parseShareLink(String shortCode);
}

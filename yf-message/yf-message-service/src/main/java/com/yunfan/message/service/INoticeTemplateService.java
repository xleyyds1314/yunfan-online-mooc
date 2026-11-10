package com.yunfan.message.service;

import com.yunfan.message.domain.dto.NoticeTemplateDTO;
import com.yunfan.message.domain.dto.NoticeTemplateDetailDTO;
import com.yunfan.message.domain.dto.NoticeTemplateFormDTO;
import com.yunfan.message.domain.query.NoticeTemplatePageQuery;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.message.domain.po.NoticeTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 通知模板 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-08-19
 */
public interface INoticeTemplateService extends IService<NoticeTemplate> {

    Long saveNoticeTemplate(NoticeTemplateFormDTO noticeTemplateFormDTO);

    void updateNoticeTemplate(NoticeTemplateFormDTO noticeTemplateFormDTO);

    PageDTO<NoticeTemplateDTO> queryNoticeTemplates(NoticeTemplatePageQuery pageQuery);

    NoticeTemplateDetailDTO queryNoticeTemplate(Long id);

    NoticeTemplate queryByCode(String code);
}

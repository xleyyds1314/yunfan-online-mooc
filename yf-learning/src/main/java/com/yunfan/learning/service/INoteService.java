package com.yunfan.learning.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.learning.domain.dto.NoteFormDTO;
import com.yunfan.learning.domain.po.Note;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.learning.domain.query.NoteAdminPageQuery;
import com.yunfan.learning.domain.query.NotePageQuery;
import com.yunfan.learning.domain.vo.NoteAdminDetailVO;
import com.yunfan.learning.domain.vo.NoteAdminVO;
import com.yunfan.learning.domain.vo.NoteVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xleyyds1314
 */
public interface INoteService extends IService<Note> {

    void saveNote(NoteFormDTO noteDTO);

    void gatherNote(Long id);

    void removeGatherNote(Long id);

    void updateNote(NoteFormDTO noteDTO);

    PageDTO<NoteVO> queryNotePage(NotePageQuery query);

    PageDTO<NoteAdminVO> queryNotePageForAdmin(NoteAdminPageQuery query);

    NoteAdminDetailVO queryNoteDetailForAdmin(Long id);

    void hiddenNote(Long id, boolean hidden);

    void removeMyNote(Long id);
}

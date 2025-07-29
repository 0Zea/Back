package org.ozea.notice.service;

import org.ozea.common.pagenation.Page;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.notice.dto.NoticeDTO;
import java.util.UUID;

public interface NoticeService {
    NoticeDTO get(UUID noticeId);
    NoticeDTO create(NoticeDTO notice);
    NoticeDTO update(UUID noticeId,NoticeDTO notice);
    NoticeDTO delete(UUID noticeId);
    Page<NoticeDTO> getPage(PageRequest pageRequest);
    Page<NoticeDTO> findByTitleContaining(String keyword, PageRequest pageRequest);
}

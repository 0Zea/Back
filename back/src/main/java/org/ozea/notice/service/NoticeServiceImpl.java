package org.ozea.notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.pagenation.Page;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.notice.domain.NoticeVO;
import org.ozea.notice.dto.NoticeDTO;
import org.ozea.notice.mapper.NoticeMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService{
    final private NoticeMapper mapper;
    @Override
    public NoticeDTO get(UUID noticeId) {

        NoticeDTO notice = NoticeDTO.of(mapper.get(noticeId));
        return Optional.ofNullable(notice).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public NoticeDTO create(NoticeDTO notice) {

        if (notice.getNoticeId() == null) {
            notice.setNoticeId(UUID.randomUUID().toString());
        }
        NoticeVO noticeVO = notice.toVo();
        mapper.create(noticeVO);
        return get(noticeVO.getNoticeId());
    }

    @Override
    public NoticeDTO update(UUID noticeId, NoticeDTO notice) {
        NoticeVO noticeVO = notice.toVo();

        mapper.update(noticeId, noticeVO);
        return get(noticeId);
    }

    @Override
    public NoticeDTO delete(UUID noticeId) {

        NoticeDTO notice = get(noticeId);
        mapper.delete(noticeId);
        return notice;
    }

    @Override
    public Page<NoticeDTO> getPage(PageRequest pageRequest) {
        List<NoticeVO> notices = mapper.getPage(pageRequest);
        int totalCount = mapper.getTotalCount();
        return Page.of(pageRequest, totalCount,
                notices.stream().map(NoticeDTO::of).toList());
    }

    @Override
    public Page<NoticeDTO> findByTitleContaining(String keyword, PageRequest pageRequest) {

        List<NoticeVO> notices = mapper.findByTitleContaining(keyword, pageRequest);
        int totalCount = mapper.getTotalCountByTitle(keyword);
        return Page.of(pageRequest, totalCount,
                notices.stream().map(NoticeDTO::of).toList());
    }
}

package org.ozea.inquiry.service;

import org.ozea.common.pagenation.Page;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.inquiry.dto.InquiryDTO;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

public interface InquiryService {
    InquiryDTO get(UUID infoId);
    InquiryDTO create(InquiryDTO inquiry);
    InquiryDTO update(UUID infoId,InquiryDTO inquiry);
    InquiryDTO updateAnswered(UUID infoId,InquiryDTO inquiry);
    InquiryDTO delete(UUID infoId);
    Page<InquiryDTO> getPage(PageRequest pageRequest);
    Page<InquiryDTO> findByTitleContaining(String keyword, PageRequest pageRequest);
    void increaseViewCount(UUID infoId);
    List<InquiryDTO> getFaqList();
}

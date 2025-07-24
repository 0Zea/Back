package org.ozea.inquiry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.pagenation.Page;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.inquiry.domain.InquiryVO;
import org.ozea.inquiry.dto.InquiryDTO;
import org.ozea.inquiry.mapper.InquiryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService{
    final private InquiryMapper mapper;

    @Override
    public InquiryDTO get(UUID no) {
        log.info("get......" + no);
        InquiryDTO inquiry = InquiryDTO.of(mapper.get(no));
        return Optional.ofNullable(inquiry).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public InquiryDTO create(InquiryDTO inquiry) {
        log.info("create......" + inquiry);
        InquiryVO inquiryVO = inquiry.toVo();
        mapper.create(inquiryVO);
//        return get(inquiryVO.get());
        return null;
    }


    @Override
    public InquiryDTO update(UUID no, InquiryDTO inquiry) {
        log.info("update........"+inquiry);
        InquiryVO inquiryVO = inquiry.toVo();
        log.info("update........"+inquiryVO);
        mapper.update(no, inquiryVO);
        return get(no);
    }

    @Override
    public InquiryDTO delete(UUID no) {
        log.info("delete........"+no);
        InquiryDTO inquiry = get(no);
        mapper.delete(no);
        return inquiry;
    }

    @Override
    public Page<InquiryDTO> getPage(PageRequest pageRequest) {
        List<InquiryVO> inquiries = mapper.getPage(pageRequest);
        int totalCount = mapper.getTotalCount();
        return Page.of(pageRequest, totalCount,
                inquiries.stream().map(InquiryDTO::of).toList());
    }

    @Override
    public Page<InquiryDTO> findByTitleContaining(String keyword, PageRequest pageRequest) {
        log.info("findByTitleContaining... title: {}", keyword);
        List<InquiryVO> inquiries = mapper.findByTitleContaining(keyword, pageRequest);
        int totalCount = mapper.getTotalCountByTitle(keyword);
        return Page.of(pageRequest, totalCount,
                inquiries.stream().map(InquiryDTO::of).toList());
    }
}

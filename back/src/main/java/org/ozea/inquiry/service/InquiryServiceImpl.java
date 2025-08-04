package org.ozea.inquiry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.pagenation.Page;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.inquiry.domain.InquiryVO;
import org.ozea.inquiry.dto.InquiryDTO;
import org.ozea.inquiry.mapper.InquiryMapper;
import org.ozea.user.domain.User;
import org.ozea.user.mapper.UserMapper;
import org.ozea.user.service.EmailService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {
    final private InquiryMapper mapper;
    final private UserMapper userMapper;
    final private EmailService emailService;

    @Override
    public InquiryDTO get(UUID infoId) {
        InquiryDTO inquiry = InquiryDTO.of(mapper.get(infoId));
        return Optional.ofNullable(inquiry).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public InquiryDTO create(InquiryDTO inquiry) {
        if (inquiry.getUserId() == null || inquiry.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }
        if (inquiry.getTitle() == null || inquiry.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목이 필요합니다.");
        }
        if (inquiry.getContent() == null || inquiry.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("내용이 필요합니다.");
        }

        if (inquiry.getUserName() == null || inquiry.getUserName().trim().isEmpty()) {
            try {
                User user = userMapper.findById(UUID.fromString(inquiry.getUserId()));
                if (user != null && user.getName() != null) {
                    inquiry.setUserName(user.getName());
                } else {
                    inquiry.setUserName("사용자");
                }
            } catch (Exception e) {
                inquiry.setUserName("사용자");
            }
        }

        if (inquiry.getInfoId() == null) {
            inquiry.setInfoId(UUID.randomUUID().toString());
        }
        
        InquiryVO inquiryVO = inquiry.toVo();
        
        mapper.create(inquiryVO);
        return get(inquiryVO.getInfoId());
    }

    @Override
    public InquiryDTO update(UUID infoId, InquiryDTO inquiry) {
        InquiryVO inquiryVO = inquiry.toVo();

        mapper.update(infoId, inquiryVO);
        return get(infoId);
    }

    @Override
    public InquiryDTO updateAnswered(UUID infoId, InquiryDTO inquiry) {
        InquiryVO inquiryVO = inquiry.toVo();

        mapper.updateAnswered(infoId, inquiryVO);
        InquiryDTO inq = get(infoId);

        if (inq.getUserId() != null) {
            User user = userMapper.findById(UUID.fromString(inq.getUserId()));
            if (user != null && user.getEmail() != null && inquiryVO.getIsAnswered() != null) {
                emailService.sendInquiryAnsweredEmail(user.getEmail(), inquiryVO.getTitle(), inquiryVO.getAnsweredContent());
            }
        }
        return inq;
    }

    @Override
    public InquiryDTO delete(UUID infoId) {
        InquiryDTO inquiry = get(infoId);
        mapper.delete(infoId);
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
        List<InquiryVO> inquiries = mapper.findByTitleContaining(keyword, pageRequest);
        int totalCount = mapper.getTotalCountByTitle(keyword);
        return Page.of(pageRequest, totalCount,
                inquiries.stream().map(InquiryDTO::of).toList());
    }

    @Override
    public void increaseViewCount(UUID infoId) {
        mapper.increaseViewCount(infoId);
    }

    @Override
    public List<InquiryDTO> getFaqList() {
        return mapper.getTopFaqInquiries().stream()
                .map(InquiryDTO::of)
                .toList();
    }
}
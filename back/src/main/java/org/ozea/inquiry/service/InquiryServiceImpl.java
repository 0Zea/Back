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
    // 기존 메서드들...

    @Override
    public InquiryDTO get(UUID infoId) {
        log.info("get......" + infoId);
        InquiryDTO inquiry = InquiryDTO.of(mapper.get(infoId));
        return Optional.ofNullable(inquiry).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public InquiryDTO create(InquiryDTO inquiry) {
        log.info("create......" + inquiry);
        if (inquiry.getInfoId() == null) {
            inquiry.setInfoId(UUID.randomUUID().toString()); // DTO가 String이므로 toString() 유지
        }
        InquiryVO inquiryVO = inquiry.toVo();
        mapper.create(inquiryVO);
        return get(inquiryVO.getInfoId()); // VO의 UUID를 String으로 변환해서 get 호출
    }

    @Override
    public InquiryDTO update(UUID infoId, InquiryDTO inquiry) {
        InquiryVO inquiryVO = inquiry.toVo();
        log.info("update vo........" + inquiryVO);
        mapper.update(infoId, inquiryVO);
        return get(infoId);
    }

    @Override
    public InquiryDTO updateAnswered(UUID infoId, InquiryDTO inquiry) {
        InquiryVO inquiryVO = inquiry.toVo();
        log.info("update answer vo........" + inquiryVO);
        mapper.updateAnswered(infoId, inquiryVO);
        InquiryDTO inq = get(infoId);

        if (inq.getUserId() != null) { // Assuming InquiryDTO has a getUserId() method
            User user = userMapper.findById(UUID.fromString(inq.getUserId())); // Use findById to get the User object
            // 최초 답변만 이메일을 보내도록 함
            if (user != null && user.getEmail() != null && inquiryVO.getIsAnswered() != null) {
                // Actual email sending
                emailService.sendInquiryAnsweredEmail(user.getEmail(), inquiryVO.getTitle(), inquiryVO.getAnsweredContent());
            } else {
                log.warn("User or user email not found for inquiryId: {}", infoId);
            }
        } else {
            log.warn("UserId not found for inquiryId: {}", infoId);
        }
        return inq;
    }

    @Override
    public InquiryDTO delete(UUID infoId) {
        log.info("delete........" + infoId);
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
        log.info("findByTitleContaining... title: {}", keyword);
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
        log.info("getFaqList......");
        return mapper.getTopFaqInquiries().stream()
                .map(InquiryDTO::of)
                .toList();
    }
}
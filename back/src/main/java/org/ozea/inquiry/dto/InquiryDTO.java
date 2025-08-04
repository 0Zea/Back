package org.ozea.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.inquiry.domain.InquiryVO;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InquiryDTO {
    private String infoId;
    private String userId;
    private String userName;
    private String content;
    private String title;
    private Boolean isAnswered;
    private Date createdAt;
    private String answeredContent;
    private Integer viewCount;

    public static InquiryDTO of(InquiryVO vo) {
        return vo == null ? null : InquiryDTO.builder()
                .infoId(vo.getInfoId().toString())
                .userId(vo.getUserId().toString())
                .userName(vo.getUserName())
                .title(vo.getTitle())
                .content(vo.getContent())
                .isAnswered(vo.getIsAnswered())
                .createdAt(vo.getCreatedAt())
                .answeredContent(vo.getAnsweredContent())
                .viewCount(vo.getViewCount())
                .build();
    }

    public InquiryVO toVo() {
        return InquiryVO.builder()
                .infoId(UUID.fromString(infoId))
                .userId(UUID.fromString(userId))
                .userName(userName)
                .title(title)
                .content(content)
                .isAnswered(isAnswered)
                .createdAt(createdAt)
                .answeredContent(answeredContent)
                .viewCount(viewCount)
                .build();
    }
}
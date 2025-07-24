package org.ozea.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.inquiry.domain.InquiryVO;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InquiryDTO {
    private String infoId;               // UUID → String
    private String userId;               // UUID → String
    private String userName;             // 조인된 user.name
    private String content;              // 문의 내용
    private String title;                // 문의 제목
    private Boolean isAnswered;          // 답변 여부 (O/X)
    private LocalDateTime createdAt;     // 등록 일자
    private String answeredContent;      // 답변 내용 (nullable)

    public static InquiryDTO of(InquiryVO vo) {
        return vo == null ? null : InquiryDTO.builder()
                .infoId(vo.getInfoId() != null ? vo.getInfoId().toString() : null)
                .userId(vo.getUserId() != null ? vo.getUserId().toString() : null)
                .userName(vo.getUserName())
                .title(vo.getTitle())
                .content(vo.getContent())
                .isAnswered(vo.getIsAnswered())
                .createdAt(vo.getCreatedAt())
                .answeredContent(vo.getAnsweredContent())
                .build();
    }

    public InquiryVO toVo() {
        return InquiryVO.builder()
                .infoId(infoId != null ? UUID.fromString(infoId) : null)
                .userId(userId != null ? UUID.fromString(userId) : null)
                .userName(userName)
                .title(title)
                .content(content)
                .isAnswered(isAnswered)
                .createdAt(createdAt)
                .answeredContent(answeredContent)
                .build();
    }
}
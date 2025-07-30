package org.ozea.inquiry.domain;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryVO {
    private UUID infoId;         // UUID → String
    private UUID userId;         // UUID → String
    private String userName;
    private String content;            // 문의 내용
    private String title;              // 문의 제목
    private Boolean isAnswered;        // 답변 여부 (O/X)
    private Date createdAt;   // 등록 일자
    private String answeredContent;    // 답변 내용 (nullable)
    private Integer viewCount;
}
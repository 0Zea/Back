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
    private UUID infoId;
    private UUID userId;
    private String userName;
    private String content;
    private String title;
    private Boolean isAnswered;
    private Date createdAt;
    private String answeredContent;
    private Integer viewCount;
}
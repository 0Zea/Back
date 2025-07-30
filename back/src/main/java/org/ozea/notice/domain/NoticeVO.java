package org.ozea.notice.domain;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeVO {
    private UUID noticeId;         // UUID → String
    private UUID adminId;         // UUID → String
    private String title;              // 문의 제목
    private String content;            // 문의 내용
    private Date createdAt;   // 등록 일자
}
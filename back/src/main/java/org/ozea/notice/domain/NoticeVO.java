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
    private UUID noticeId;
    private UUID adminId;
    private String title;
    private String content;
    private Date createdAt;
}
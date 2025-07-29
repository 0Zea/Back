package org.ozea.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.notice.domain.NoticeVO;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeDTO {
    private String noticeId;               // UUID → String
    private String adminId;               // UUID → String
    private String content;              // 문의 내용
    private String title;                // 문의 제목
    private Date createdAt;     // 등록 일자

    public static NoticeDTO of(NoticeVO vo) {
        return vo == null ? null : NoticeDTO.builder()
                .noticeId(vo.getNoticeId().toString())
                .adminId(vo.getAdminId().toString())
                .title(vo.getTitle())
                .content(vo.getContent())
                .createdAt(vo.getCreatedAt())
                .build();
    }

    public NoticeVO toVo() {
        return NoticeVO.builder()
                .noticeId(UUID.fromString(noticeId))
                .adminId(adminId != null ? UUID.fromString(adminId) : null)
                .title(title)
                .content(content)
                .createdAt(createdAt)
                .build();
    }
}
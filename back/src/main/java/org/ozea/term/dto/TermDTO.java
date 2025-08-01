package org.ozea.term.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.term.domain.TermVO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TermDTO {
    private Integer id;
    private String title;
    private String description;
    private String category;
    public static TermDTO of(TermVO vo) {
        return vo == null ? null : TermDTO.builder()
                .id(vo.getId())
                .title(vo.getTitle())
                .description(vo.getDescription())
                .category(vo.getCategory())
                .build();
    }

    public TermVO toVo() {
        return TermVO.builder()
                .id(id)
                .title(title)
                .description(description)
                .category(category)
                .build();
    }
}

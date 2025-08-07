package org.ozea.term.domain;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermVO {
    private int id;
    private String title;
    private String description;
    private String category;
}

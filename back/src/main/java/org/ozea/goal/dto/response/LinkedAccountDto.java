package org.ozea.goal.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
@Getter
@Builder
@AllArgsConstructor
public class LinkedAccountDto {
    private int account_id;
    private String bank_name;
    private String account_num;
    private String account_type;
    private Long balance;
}

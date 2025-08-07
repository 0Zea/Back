package org.ozea.bank.domain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankCode {
    private String bankCode;
    private String bankName;
    private String bankIcon;
}
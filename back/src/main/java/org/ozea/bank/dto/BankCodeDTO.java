package org.ozea.bank.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.bank.domain.BankCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankCodeDTO {
    private String bankCode;
    private String bankName;
    private String bankIcon;
    public static BankCodeDTO of(BankCode bankCode) {
        return BankCodeDTO.builder()
                .bankCode(bankCode.getBankCode())
                .bankName(bankCode.getBankName())
                .bankIcon(bankCode.getBankIcon())
                .build();
    }
    public static List<BankCodeDTO> of(List<BankCode> bankCodeList) {
        return bankCodeList.stream()
                .map(BankCodeDTO::of)
                .collect(Collectors.toList());
    }
    public BankCode toEntity() {
        return BankCode.builder()
                .bankCode(bankCode)
                .bankName(bankName)
                .bankIcon(bankIcon)
                .build();
    }
}
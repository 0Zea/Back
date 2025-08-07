package org.ozea.asset.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.asset.domain.BankAccountVO;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDTO {
    private String bankName;
    private String accountNum;
    private String accountType;
    private BigInteger balance;
    public static BankAccountDTO of(BankAccountVO vo) {
        return BankAccountDTO.builder()
                .bankName(vo.getBankName())
                .accountNum(vo.getAccountNum())
                .accountType(vo.getAccountType())
                .balance(vo.getBalance())
                .build();
    }
    public static List<BankAccountDTO> of(List<BankAccountVO> voList) {
        return voList.stream()
                .map(BankAccountDTO::of)
                .collect(Collectors.toList());
    }
    public BankAccountVO toVO(UUID userId) {
        return BankAccountVO.builder()
                .userId(userId)
                .bankName(bankName)
                .accountNum(this.accountNum)
                .accountType(this.accountType)
                .balance(this.balance)
                .build();
    }
}

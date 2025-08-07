package org.ozea.asset.domain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigInteger;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankAccountVO {
    private UUID userId;
    private String bankName;
    private String accountNum;
    private String accountType;
    private BigInteger balance;
}

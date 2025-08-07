package org.ozea.bank.mapper;
import org.ozea.bank.domain.BankCode;
import java.util.List;
public interface BankCodeMapper {
    List<BankCode> findAllActive();
    BankCode findByBankCode(String bankCode);
    void insertBankCode(BankCode bankCode);
    void updateBankCode(BankCode bankCode);
    void deleteBankCode(String bankCode);
}
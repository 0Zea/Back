package org.ozea.bank.service;
import org.ozea.bank.dto.BankCodeDTO;
import java.util.List;
public interface BankCodeService {
    List<BankCodeDTO> getAllActiveBanks();
    BankCodeDTO getBankByCode(String bankCode);
    void addBank(BankCodeDTO bankCodeDTO);
    void updateBank(BankCodeDTO bankCodeDTO);
    void deleteBank(String bankCode);
}
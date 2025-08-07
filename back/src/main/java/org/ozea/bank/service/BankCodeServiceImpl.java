package org.ozea.bank.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.bank.domain.BankCode;
import org.ozea.bank.dto.BankCodeDTO;
import org.ozea.bank.mapper.BankCodeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Log4j2
@Service
@RequiredArgsConstructor
public class BankCodeServiceImpl implements BankCodeService {
    private final BankCodeMapper bankCodeMapper;
    @Override
    public List<BankCodeDTO> getAllActiveBanks() {
        List<BankCode> banks = bankCodeMapper.findAllActive();
        return BankCodeDTO.of(banks);
    }
    @Override
    public BankCodeDTO getBankByCode(String bankCode) {
        BankCode bank = bankCodeMapper.findByBankCode(bankCode);
        if (bank == null) {
            throw new IllegalArgumentException("해당 은행 코드를 찾을 수 없습니다: " + bankCode);
        }
        return BankCodeDTO.of(bank);
    }
    @Override
    @Transactional
    public void addBank(BankCodeDTO bankCodeDTO) {
        BankCode bank = bankCodeDTO.toEntity();
        bankCodeMapper.insertBankCode(bank);
    }
    @Override
    @Transactional
    public void updateBank(BankCodeDTO bankCodeDTO) {
        BankCode bank = bankCodeDTO.toEntity();
        bankCodeMapper.updateBankCode(bank);
    }
    @Override
    @Transactional
    public void deleteBank(String bankCode) {
        bankCodeMapper.deleteBankCode(bankCode);
    }
}
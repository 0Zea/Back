package org.ozea.asset.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.asset.domain.BankAccountVO;
import org.ozea.asset.dto.AssetDTO;
import org.ozea.asset.dto.BankAccountDTO;
import org.ozea.asset.mapper.AssetMapper;
import org.ozea.asset.domain.AssetVO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
@Service
@Log4j2
@RequiredArgsConstructor
public class AssetImpl implements AssetService {
    private final AssetMapper assetMapper;
    @Override
    public AssetDTO getUserAssetSummary(UUID userId) {
        AssetVO assetVO = assetMapper.getUserAssetSummary(userId);
        if (assetVO == null) {
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없음");
        }
        AssetDTO dto = AssetDTO.of(assetVO);
        return dto;
    }
    @Override
    public List<BankAccountDTO> getUserBankAccounts(UUID userId) {
        List<BankAccountVO> voList = assetMapper.getUserBankAccounts(userId);
        List<BankAccountDTO> dtoList = BankAccountDTO.of(voList);
        return dtoList;
    }
}

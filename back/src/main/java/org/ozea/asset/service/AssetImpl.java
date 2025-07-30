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

        log.info("사용자 자산 요약 조회 시작: userId = {}", userId);

        // mapper를 통한 VO 조회
        AssetVO assetVO = assetMapper.getUserAssetSummary(userId);
        log.debug("DB 조회 결과: {}", assetVO);

        // VO없을 경우 예외처리
        if (assetVO == null) {
            log.warn("사용자 데이터가 없습니다: userId = {}", userId);
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없음");
        }

        AssetDTO dto = AssetDTO.of(assetVO);
        log.info("사용자 자산 요약 조회 완료: name = {}, totalAssets = {}",
                dto.getName(), dto.getTotalAssets());

        return dto;
    }

    @Override
    public List<BankAccountDTO> getUserBankAccounts(UUID userId) {
        log.info("사용자 계좌 목록 조회 시작: userId = {}", userId);

        // mapper를 통한 VO List 조회
        List<BankAccountVO> voList = assetMapper.getUserBankAccounts(userId);
        log.debug("DB 조회 결과: {} 개의 계좌", voList.size());

        // VO List -> DTO List 변환
        List<BankAccountDTO> dtoList = BankAccountDTO.of(voList);

        log.info("사용자 계좌 목록 조회 완료: {} 개의 계좌", dtoList.size());

        return dtoList;
    }
}

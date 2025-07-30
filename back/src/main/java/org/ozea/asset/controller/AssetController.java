package org.ozea.asset.controller;

import lombok.extern.log4j.Log4j2;
import org.ozea.asset.dto.AssetDTO;
import org.ozea.asset.dto.BankAccountDTO;
import org.ozea.asset.service.AssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Log4j2
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/{userId}/summary")
    public ResponseEntity<AssetDTO> getUserAssetSummary(@PathVariable UUID userId) {
        log.info("사용자 자산 요약 API 호출: userId = {}", userId);
        AssetDTO assetDTO = assetService.getUserAssetSummary(userId);
        return ResponseEntity.ok(assetDTO);
    }

    @GetMapping("/{userId}/accounts")
    public ResponseEntity<List<BankAccountDTO>> getUserBankAccounts(@PathVariable UUID userId) {
        log.info("사용자 계좌 목록 API 호출: userId = {}", userId);
        List<BankAccountDTO> accounts = assetService.getUserBankAccounts(userId);
        return ResponseEntity.ok(accounts);
    }
}

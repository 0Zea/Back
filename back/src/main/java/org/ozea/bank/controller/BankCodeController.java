package org.ozea.bank.controller;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.bank.dto.BankCodeDTO;
import org.ozea.bank.service.BankCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
@Log4j2
@Api(tags = "BankCode")
public class BankCodeController {
    private final BankCodeService bankCodeService;
    @GetMapping
    @ApiOperation(value = "은행 목록 조회", notes = "활성화된 모든 은행 목록을 조회합니다.")
    public ResponseEntity<List<BankCodeDTO>> getAllBanks() {
        List<BankCodeDTO> banks = bankCodeService.getAllActiveBanks();
        return ResponseEntity.ok(banks);
    }
    @GetMapping("/{bankCode}")
    @ApiOperation(value = "은행 정보 조회", notes = "특정 은행 코드의 정보를 조회합니다.")
    public ResponseEntity<BankCodeDTO> getBankByCode(@PathVariable String bankCode) {
        BankCodeDTO bank = bankCodeService.getBankByCode(bankCode);
        return ResponseEntity.ok(bank);
    }
    @PostMapping
    @ApiOperation(value = "은행 추가", notes = "새로운 은행을 추가합니다.")
    public ResponseEntity<Map<String, String>> addBank(@RequestBody BankCodeDTO bankCodeDTO) {
        bankCodeService.addBank(bankCodeDTO);
        return ResponseEntity.ok(Map.of("message", "은행이 성공적으로 추가되었습니다."));
    }
    @PutMapping("/{bankCode}")
    @ApiOperation(value = "은행 정보 수정", notes = "기존 은행 정보를 수정합니다.")
    public ResponseEntity<Map<String, String>> updateBank(
            @PathVariable String bankCode,
            @RequestBody BankCodeDTO bankCodeDTO) {
        bankCodeDTO.setBankCode(bankCode);
        bankCodeService.updateBank(bankCodeDTO);
        return ResponseEntity.ok(Map.of("message", "은행 정보가 성공적으로 수정되었습니다."));
    }
    @DeleteMapping("/{bankCode}")
    @ApiOperation(value = "은행 삭제", notes = "은행을 삭제합니다.")
    public ResponseEntity<Map<String, String>> deleteBank(@PathVariable String bankCode) {
        bankCodeService.deleteBank(bankCode);
        return ResponseEntity.ok(Map.of("message", "은행이 성공적으로 삭제되었습니다."));
    }
}
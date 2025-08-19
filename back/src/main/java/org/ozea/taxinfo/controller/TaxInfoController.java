package org.ozea.taxinfo.controller;

import org.ozea.taxinfo.dto.TaxInfoReqDto;
import org.ozea.taxinfo.service.TaxInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/taxinfo")
public class TaxInfoController {
    private final TaxInfoService service;

    public TaxInfoController(TaxInfoService service) {
        this.service = service;
    }

    @PostMapping("/save-and-summary")
    public ResponseEntity<Map<String,String>> saveAndSummary(@RequestBody TaxInfoReqDto request) {
        if (request == null || request.getUserId() == null || request.getYear() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.saveAndSummary(request));
    }
}

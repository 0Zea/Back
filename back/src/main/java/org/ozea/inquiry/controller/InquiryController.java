package org.ozea.inquiry.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.pagenation.Page;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.inquiry.dto.InquiryDTO;
import org.ozea.inquiry.service.InquiryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
@Log4j2
public class InquiryController {
    private final InquiryService service;

    @GetMapping("")
    public ResponseEntity<Page> getList(PageRequest pageRequest) {
        return ResponseEntity.ok(service.getPage(pageRequest));
    }

    @GetMapping("/faq")
    public ResponseEntity<List<InquiryDTO>> getFaqList() {
        return ResponseEntity.ok(service.getFaqList());
    }

    @GetMapping("/{infoId}")
    public ResponseEntity<InquiryDTO> getById(@PathVariable UUID infoId){
        return ResponseEntity.ok(service.get(infoId));
    }

    @PatchMapping("/{infoId}/increaseViewCount")
    public ResponseEntity<Void> increaseViewCount(@PathVariable UUID infoId) {
        service.increaseViewCount(infoId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("")
    public ResponseEntity<InquiryDTO> create(@RequestBody InquiryDTO inquiry){
        return ResponseEntity.ok(service.create(inquiry));
    }

    @PatchMapping("/{infoId}")
    public ResponseEntity<InquiryDTO> update(@PathVariable UUID infoId, @RequestBody InquiryDTO inquiry){
        return ResponseEntity.ok(service.update(infoId, inquiry));
    }

    @PatchMapping("/{infoId}/answer")
    public ResponseEntity<InquiryDTO> updateAnswered(@PathVariable UUID infoId, @RequestBody InquiryDTO inquiry){
        return ResponseEntity.ok(service.updateAnswered(infoId, inquiry));
    }

    @DeleteMapping("/{infoId}")
    public ResponseEntity<InquiryDTO> delete(@PathVariable UUID infoId){
        return ResponseEntity.ok(service.delete(infoId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page> searchInquiries(
            @RequestParam("keyword") String keyword,
            PageRequest pageRequest) {
        return ResponseEntity.ok(service.findByTitleContaining(keyword, pageRequest));
    }
}
package org.ozea.inquiry.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.annotations.Delete;
import org.ozea.common.pagenation.Page;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.inquiry.dto.InquiryDTO;
import org.ozea.inquiry.service.InquiryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

//http://localhost:8080/api/travel?page=1&amount=12
@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
@Log4j2
public class InquiryController {
    private final InquiryService service;
    //리스트 조회
    @GetMapping("")
    public ResponseEntity<Page> getList(PageRequest pageRequest) {
        return ResponseEntity.ok(service.getPage(pageRequest));
    }
    //상세보기
    @GetMapping("/{no}")
    public ResponseEntity<InquiryDTO> getById(@PathVariable UUID no){
        return ResponseEntity.ok(service.get(no));
    }

    //생성
    @PostMapping("")
    public ResponseEntity<InquiryDTO> create(@RequestBody InquiryDTO inquiry){
        return ResponseEntity.ok(service.create(inquiry));
    }
    @PatchMapping("/{no}")
    public ResponseEntity<InquiryDTO> update(@PathVariable UUID no,@RequestBody InquiryDTO inquiry){
        return ResponseEntity.ok(service.update(no, inquiry));
    }
    //삭제
    @DeleteMapping("/{no}")
    public ResponseEntity<InquiryDTO> delete(@PathVariable UUID no){
        return ResponseEntity.ok(service.delete(no));
    }
    //검색 리스트 조회
    @GetMapping("/search")
    public ResponseEntity<Page> searchInquiries(
            @RequestParam("keyword") String keyword,
            PageRequest pageRequest) {
        return ResponseEntity.ok(service.findByTitleContaining(keyword, pageRequest));
    }
}

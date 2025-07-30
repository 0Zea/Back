package org.ozea.notice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.pagenation.Page;
import org.ozea.common.pagenation.PageRequest;
import org.ozea.notice.dto.NoticeDTO;
import org.ozea.notice.service.NoticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
@Log4j2
public class NoticeController {
    private final NoticeService service;
    //공지사항 리스트 조회
    @GetMapping("")
    public ResponseEntity<Page> getList(PageRequest pageRequest) {
        return ResponseEntity.ok(service.getPage(pageRequest));
    }
    //공지사항 상세보기
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDTO> getById(@PathVariable UUID noticeId){
        return ResponseEntity.ok(service.get(noticeId));
    }
    //공지사항 생성
    @PostMapping("")
    public ResponseEntity<NoticeDTO> create(@RequestBody NoticeDTO notice){
        return ResponseEntity.ok(service.create(notice));
    }
    //공지사항 수정
    @PatchMapping("/{noticeId}")
    public ResponseEntity<NoticeDTO> update(@PathVariable UUID noticeId, @RequestBody NoticeDTO notice){
        return ResponseEntity.ok(service.update(noticeId, notice));
    }
    //삭제
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<NoticeDTO> delete(@PathVariable UUID noticeId){
        return ResponseEntity.ok(service.delete(noticeId));
    }
    //검색 리스트 조회
    @GetMapping("/search")
    public ResponseEntity<Page> searchInquiries(
            @RequestParam("keyword") String keyword,
            PageRequest pageRequest) {
        return ResponseEntity.ok(service.findByTitleContaining(keyword, pageRequest));
    }
}

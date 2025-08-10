package org.ozea.term.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.term.dto.TermDTO;
import org.ozea.term.service.TermService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequestMapping("/api/term")
@RequiredArgsConstructor
@Log4j2
public class TermController {
    private final TermService service;
    @GetMapping("")
    public ResponseEntity<List<TermDTO>> getList() {
        return ResponseEntity.ok(service.getList());
    }
}

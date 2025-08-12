package org.ozea.common.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Map;

@RestController
class DebugController {
    private final ObjectMapper om;
    DebugController(ObjectMapper om){ this.om = om; }

    @GetMapping("/debug/date")
    public Map<String,Object> date() {
        return Map.of("today", java.time.LocalDate.now());
    }

    @PostConstruct
    void log() { // 어떤 모듈들 올라왔는지 로그
        System.out.println("ObjectMapper modules -> " + om.getRegisteredModuleIds());
    }
}
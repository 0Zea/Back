package org.ozea.point.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Point {
    private UUID pointId;
    private UUID userId;
    private Integer pointAmount;
    private String typeDetail; // 상세 내역
    private LocalDateTime createdAt;
    private Integer totalAmount;
    private Integer type; // 1: 적립, 2: 출금
} 
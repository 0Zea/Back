package org.ozea.point.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.point.domain.Point;
import org.ozea.point.dto.PointDTO;
import org.ozea.point.mapper.PointMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointMapper pointMapper;

    @Override
    public List<PointDTO> getPointHistory(UUID userId) {
        List<Point> points = pointMapper.findByUserId(userId);
        return PointDTO.of(points);
    }

    @Override
    public List<PointDTO> getPointHistoryByType(UUID userId, String type) {
        Integer typeInt = "적립".equals(type) ? 1 : 2;
        List<Point> points = pointMapper.findByUserIdAndType(userId, typeInt);
        return PointDTO.of(points);
    }

    @Override
    public Integer getTotalPoints(UUID userId) {
        return pointMapper.getTotalPointsByUserId(userId);
    }

    @Override
    @Transactional
    public void addPoints(UUID userId, Integer amount, String reason) {
        Integer currentTotal = getTotalPoints(userId);
        Integer newTotal = currentTotal + amount;

        Point point = Point.builder()
                .pointId(UUID.randomUUID())
                .userId(userId)
                .pointAmount(amount)
                .typeDetail(reason)
                .createdAt(LocalDateTime.now())
                .totalAmount(newTotal)
                .type(1) // 적립
                .build();

        pointMapper.insertPoint(point);
        log.info("포인트 적립 완료: 사용자={}, 금액={}, 사유={}", userId, amount, reason);
    }

    @Override
    @Transactional
    public void withdrawPoints(UUID userId, Integer amount, String reason) {
        Integer currentTotal = getTotalPoints(userId);
        
        // 최소 출금 금액 검증 (10000원 이상)
        if (amount < 10000) {
            throw new IllegalArgumentException("출금은 10,000원 이상부터 가능합니다.");
        }
        
        // 보유 포인트 검증
        if (currentTotal < amount) {
            throw new IllegalArgumentException("보유 포인트가 부족합니다. 현재 보유: " + currentTotal + "P");
        }

        Integer newTotal = currentTotal - amount;

        Point point = Point.builder()
                .pointId(UUID.randomUUID())
                .userId(userId)
                .pointAmount(amount)
                .typeDetail(reason)
                .createdAt(LocalDateTime.now())
                .totalAmount(newTotal)
                .type(2) // 출금
                .build();

        pointMapper.insertPoint(point);
        log.info("포인트 출금 완료: 사용자={}, 금액={}, 사유={}", userId, amount, reason);
    }
} 
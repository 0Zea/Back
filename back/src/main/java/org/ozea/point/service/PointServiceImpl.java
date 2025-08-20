package org.ozea.point.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ozea.common.cache.CacheHelper;
import org.ozea.point.domain.Point;
import org.ozea.point.dto.PointDTO;
import org.ozea.point.mapper.PointMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointMapper pointMapper;
    private final CacheHelper cacheHelper;

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
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("적립 포인트는 1P 이상이어야 합니다.");
        }

        String lockKey = "lock:points:" + userId;
        boolean locked = cacheHelper.tryLock(lockKey, Duration.ofSeconds(5));
        if (!locked) {
            throw new IllegalStateException("포인트 처리 중입니다. 잠시 후 다시 시도하세요.");
        }

        try {
            Integer currentTotal = getTotalPoints(userId);
            int base = (currentTotal == null ? 0 : currentTotal);
            int newTotal = base + amount;

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
        } finally {
            cacheHelper.unlock(lockKey);
        }
    }

    @Override
    @Transactional
    public void withdrawPoints(UUID userId, Integer amount, String reason) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("출금 포인트는 1P 이상이어야 합니다.");
        }
        if (amount < 10_000) {
            throw new IllegalArgumentException("출금은 10,000원 이상부터 가능합니다.");
        }

        String lockKey = "lock:points:" + userId;
        boolean locked = cacheHelper.tryLock(lockKey, Duration.ofSeconds(5));
        if (!locked) {
            throw new IllegalStateException("포인트 처리 중입니다. 잠시 후 다시 시도하세요.");
        }

        try {
            Integer currentTotal = getTotalPoints(userId);
            int base = (currentTotal == null ? 0 : currentTotal);

            if (base < amount) {
                throw new IllegalArgumentException("보유 포인트가 부족합니다. 현재 보유: " + base + "P");
            }

            int newTotal = base - amount;

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
        } finally {
            cacheHelper.unlock(lockKey);
        }
    }
}
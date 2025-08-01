package org.ozea.point.service;

import org.ozea.point.dto.PointDTO;

import java.util.List;
import java.util.UUID;

public interface PointService {
    List<PointDTO> getPointHistory(UUID userId);
    List<PointDTO> getPointHistoryByType(UUID userId, String type);
    Integer getTotalPoints(UUID userId);
    void addPoints(UUID userId, Integer amount, String reason);
    void withdrawPoints(UUID userId, Integer amount, String reason);
} 
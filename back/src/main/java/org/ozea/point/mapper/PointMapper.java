package org.ozea.point.mapper;

import org.apache.ibatis.annotations.Param;
import org.ozea.point.domain.Point;

import java.util.List;
import java.util.UUID;

public interface PointMapper {
    List<Point> findByUserId(UUID userId);
    List<Point> findByUserIdAndType(@Param("userId") UUID userId, @Param("type") Integer type);
    void insertPoint(Point point);
    Integer getTotalPointsByUserId(UUID userId);
} 
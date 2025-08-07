package org.ozea.point.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ozea.point.domain.Point;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointDTO {
    private UUID pointId;
    private UUID userId;
    private Integer pointAmount;
    private String typeDetail;
    private LocalDateTime createdAt;
    private Integer totalAmount;
    private Integer type;
    public static PointDTO of(Point point) {
        return PointDTO.builder()
                .pointId(point.getPointId())
                .userId(point.getUserId())
                .pointAmount(point.getPointAmount())
                .typeDetail(point.getTypeDetail())
                .createdAt(point.getCreatedAt())
                .totalAmount(point.getTotalAmount())
                .type(point.getType())
                .build();
    }
    public static List<PointDTO> of(List<Point> pointList) {
        return pointList.stream()
                .map(PointDTO::of)
                .collect(Collectors.toList());
    }
    public Point toEntity() {
        return Point.builder()
                .pointId(pointId)
                .userId(userId)
                .pointAmount(pointAmount)
                .typeDetail(typeDetail)
                .createdAt(createdAt)
                .totalAmount(totalAmount)
                .type(type)
                .build();
    }
}
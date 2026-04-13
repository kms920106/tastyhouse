package com.tastyhouse.webapi.member.response;

import com.tastyhouse.core.entity.point.MemberPoint;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 응답 DTO")
public record PointResponse(
    @Schema(description = "사용 가능 포인트", example = "1000")
    Integer availablePoints,

    @Schema(description = "이번달 소멸 예정 포인트", example = "0")
    Integer expiredThisMonth
) {
    public static PointResponse from(MemberPoint memberPoint) {
        return new PointResponse(
            memberPoint.getAvailablePoints(),
            memberPoint.getExpiredThisMonth()
        );
    }

    public static PointResponse of(Integer availablePoints, Integer expiredThisMonth) {
        return new PointResponse(
            availablePoints,
            expiredThisMonth
        );
    }
}

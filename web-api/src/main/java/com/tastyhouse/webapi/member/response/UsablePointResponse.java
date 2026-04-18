package com.tastyhouse.webapi.member.response;

import com.tastyhouse.core.entity.point.MemberPoint;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용 가능 포인트 응답 DTO (주문용)")
public record UsablePointResponse(
    @Schema(description = "사용 가능 포인트", example = "1000")
    Integer usablePoints
) {
    public static UsablePointResponse from(MemberPoint memberPoint) {
    return new UsablePointResponse(memberPoint.getAvailablePoints());
    }

    public static UsablePointResponse of(Integer usablePoints) {
    return new UsablePointResponse(usablePoints);
    }
}

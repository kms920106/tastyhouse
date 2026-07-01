package com.tastyhouse.webapi.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.point.application.dto.result.MemberPointResult;

@Schema(description = "사용 가능 포인트 응답 DTO (주문용)")
public record UsablePointResponse(
    @Schema(description = "사용 가능 포인트", example = "1000")
    Integer usablePoints
) {
    public static UsablePointResponse from(MemberPointResult result) {
        return new UsablePointResponse(result.availablePoints());
    }

    public static UsablePointResponse of(Integer usablePoints) {
        return new UsablePointResponse(usablePoints);
    }
}

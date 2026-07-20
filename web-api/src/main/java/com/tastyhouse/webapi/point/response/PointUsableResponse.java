package com.tastyhouse.webapi.point.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용 가능 포인트 응답 DTO (주문용)")
public record PointUsableResponse(
    @Schema(description = "사용 가능 포인트", example = "1000")
    Integer usablePoints
) {
    public static PointUsableResponse of(Integer usablePoints) {
        return new PointUsableResponse(usablePoints);
    }
}

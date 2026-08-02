package com.tastyhouse.adminapi.point.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 이력 검색 요청")
public record PointSearchRequest(
    @Schema(description = "포인트 유형 (미지정 시 전체)", example = "EARNED", allowableValues = {"EARNED", "USE", "REFUND"})
    String type
) {
}

package com.tastyhouse.adminapi.coupon.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쿠폰 검색 요청")
public record CouponSearchRequest(
    @Schema(description = "쿠폰 이름 (부분 일치 검색)", example = "신규 가입")
    String name,

    @Schema(description = "할인 유형 (미지정 시 전체 유형 조회)", example = "AMOUNT", allowableValues = {"AMOUNT", "RATE"})
    String discountType,

    @Schema(description = "노출 여부 (null=전체/true=노출/false=비노출)", example = "true")
    Boolean visible
) {
}

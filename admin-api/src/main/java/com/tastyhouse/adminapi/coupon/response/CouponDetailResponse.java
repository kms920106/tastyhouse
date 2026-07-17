package com.tastyhouse.adminapi.coupon.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쿠폰 상세 응답")
public record CouponDetailResponse(
    @Schema(description = "쿠폰 ID", example = "1")
    Long id,

    @Schema(description = "쿠폰 이름", example = "신규 가입 5,000원 할인")
    String name,

    @Schema(description = "쿠폰 설명", example = "신규 가입 회원 대상 5,000원 할인 쿠폰")
    String description,

    @Schema(description = "할인 유형 (AMOUNT: 정액, RATE: 정률)", example = "AMOUNT")
    String discountType,

    @Schema(description = "할인 금액 또는 할인율(%)", example = "5000")
    Integer discountAmount,

    @Schema(description = "최대 할인 금액 (RATE 할인 시 상한)", example = "10000")
    Integer maxDiscountAmount,

    @Schema(description = "최소 주문 금액", example = "20000")
    Integer minOrderAmount,

    @Schema(description = "최대 발급 수량 (미지정 시 무제한)", example = "1000")
    Integer maxDiscountCount,

    @Schema(description = "발급 시작 일시", example = "2026-01-01T00:00:00")
    LocalDateTime issueStartAt,

    @Schema(description = "발급 종료 일시", example = "2026-01-31T23:59:59")
    LocalDateTime issueEndAt,

    @Schema(description = "사용 가능 시작 일시", example = "2026-01-01T00:00:00")
    LocalDateTime useStartAt,

    @Schema(description = "사용 가능 종료 일시", example = "2026-02-28T23:59:59")
    LocalDateTime useEndAt,

    @Schema(description = "노출 여부", example = "true")
    boolean visible,

    @Schema(description = "생성일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정일시", example = "2026-01-01T00:00:00")
    LocalDateTime updatedAt
) {
    public static CouponDetailResponse from(
        Long id,
        String name,
        String description,
        String discountType,
        Integer discountAmount,
        Integer maxDiscountAmount,
        Integer minOrderAmount,
        Integer maxDiscountCount,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt,
        boolean visible,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new CouponDetailResponse(
            id,
            name,
            description,
            discountType,
            discountAmount,
            maxDiscountAmount,
            minOrderAmount,
            maxDiscountCount,
            issueStartAt,
            issueEndAt,
            useStartAt,
            useEndAt,
            visible,
            createdAt,
            updatedAt
        );
    }
}

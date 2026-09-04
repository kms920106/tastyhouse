package com.tastyhouse.webapi.member.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.coupon.port.out.MyCouponListItemResult;

@Schema(description = "내 쿠폰 목록 아이템")
public record MyCouponListItemResponse(
    @Schema(description = "회원 쿠폰 ID(PK)", example = "1")
    Long id,

    @Schema(description = "쿠폰 ID(PK)", example = "1")
    Long couponId,

    @Schema(description = "쿠폰명", example = "신규가입 5000원 할인 쿠폰")
    String name,

    @Schema(description = "쿠폰 설명", example = "전 매장 5000원 즉시 할인")
    String description,

    @Schema(description = "할인 유형", example = "FIXED_AMOUNT")
    String discountType,

    @Schema(description = "할인 금액(또는 할인율)", example = "5000")
    Integer discountAmount,

    @Schema(description = "최대 할인 금액", example = "10000")
    Integer maxDiscountAmount,

    @Schema(description = "최소 주문 금액", example = "20000")
    Integer minOrderAmount,

    @Schema(description = "사용 가능 시작 일시", example = "2026-01-01T00:00:00")
    LocalDateTime useStartAt,

    @Schema(description = "사용 가능 종료 일시", example = "2026-12-31T23:59:59")
    LocalDateTime useEndAt,

    @Schema(description = "만료 일시", example = "2026-12-31T23:59:59")
    LocalDateTime expiredAt,

    @Schema(description = "사용 여부", example = "false")
    boolean used,

    @Schema(description = "사용 일시", example = "2026-01-05T12:30:00")
    LocalDateTime usedAt,

    @Schema(description = "만료까지 남은 일수(미사용·만료 전만 계산, 그 외 null)", example = "5")
    Long daysRemaining,

    @Schema(description = "만료 여부", example = "false")
    boolean expired
) {
    public static MyCouponListItemResponse from(MyCouponListItemResult result) {
        return new MyCouponListItemResponse(
            result.id(),
            result.couponId(),
            result.name(),
            result.description(),
            result.discountType(),
            result.discountAmount(),
            result.maxDiscountAmount(),
            result.minOrderAmount(),
            result.useStartAt(),
            result.useEndAt(),
            result.expiredAt(),
            result.used(),
            result.usedAt(),
            result.daysRemaining(),
            result.expired()
        );
    }
}

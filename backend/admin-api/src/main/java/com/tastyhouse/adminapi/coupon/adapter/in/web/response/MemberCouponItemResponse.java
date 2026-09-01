package com.tastyhouse.adminapi.coupon.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.coupon.port.out.MemberCouponItemResult;

@Schema(description = "쿠폰 발급 현황 항목 응답")
public record MemberCouponItemResponse(
    @Schema(description = "회원 쿠폰 ID", example = "1")
    Long id,

    @Schema(description = "발급받은 회원 ID", example = "1")
    Long memberId,

    @Schema(description = "사용 여부", example = "false")
    boolean used,

    @Schema(description = "사용 일시 (미사용 시 null)", example = "2026-01-15T12:30:00")
    LocalDateTime usedAt,

    @Schema(description = "만료 일시", example = "2026-02-28T23:59:59")
    LocalDateTime expiredAt,

    @Schema(description = "발급 일시", example = "2026-01-01T00:00:00")
    LocalDateTime issuedAt
) {
    public static MemberCouponItemResponse from(MemberCouponItemResult result) {
        return new MemberCouponItemResponse(
            result.id(),
            result.memberId(),
            result.used(),
            result.usedAt(),
            result.expiredAt(),
            result.createdAt()
        );
    }
}

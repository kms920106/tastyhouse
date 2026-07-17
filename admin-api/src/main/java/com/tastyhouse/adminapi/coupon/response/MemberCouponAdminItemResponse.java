package com.tastyhouse.adminapi.coupon.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.coupon.application.dto.MemberCouponItemDto;

@Schema(description = "쿠폰 발급 현황 항목 응답")
public record MemberCouponAdminItemResponse(
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
    public static MemberCouponAdminItemResponse from(MemberCouponItemDto dto) {
        return new MemberCouponAdminItemResponse(
            dto.id(),
            dto.memberId().value(),
            dto.used(),
            dto.usedAt(),
            dto.expiredAt(),
            dto.createdAt()
        );
    }
}

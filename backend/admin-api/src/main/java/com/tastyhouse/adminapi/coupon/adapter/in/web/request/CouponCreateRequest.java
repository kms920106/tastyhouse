package com.tastyhouse.adminapi.coupon.adapter.in.web.request;

import com.tastyhouse.application.coupon.port.in.CouponCreateCommand;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "쿠폰 등록 요청")
public record CouponCreateRequest(
    @NotBlank(message = "쿠폰 이름은 필수입니다.")
    @Size(max = 200, message = "쿠폰 이름은 200자를 초과할 수 없습니다.")
    @Schema(description = "쿠폰 이름", example = "신규 가입 5,000원 할인", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Size(max = 500, message = "쿠폰 설명은 500자를 초과할 수 없습니다.")
    @Schema(description = "쿠폰 설명", example = "신규 가입 회원 대상 5,000원 할인 쿠폰")
    String description,

    @NotBlank(message = "할인 유형은 필수입니다.")
    @Schema(description = "할인 유형 (AMOUNT: 정액, RATE: 정률)", example = "AMOUNT", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"AMOUNT", "RATE"})
    String discountType,

    @NotNull(message = "할인 금액(또는 할인율)은 필수입니다.")
    @Min(value = 1, message = "할인 금액(또는 할인율)은 1 이상이어야 합니다.")
    @Schema(description = "할인 금액(AMOUNT) 또는 할인율(RATE, %)", example = "5000", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer discountAmount,

    @Schema(description = "최대 할인 금액 (RATE 할인 시 상한, 미지정 시 무제한)", example = "10000")
    Integer maxDiscountAmount,

    @Min(value = 0, message = "최소 주문 금액은 0 이상이어야 합니다.")
    @Schema(description = "최소 주문 금액 (미지정 시 0)", example = "20000")
    Integer minOrderAmount,

    @Schema(description = "최대 발급 수량 (미지정 시 무제한)", example = "1000")
    Integer maxDiscountCount,

    @NotNull(message = "발급 시작 일시는 필수입니다.")
    @Schema(description = "발급 시작 일시", example = "2026-01-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime issueStartAt,

    @NotNull(message = "발급 종료 일시는 필수입니다.")
    @Schema(description = "발급 종료 일시", example = "2026-01-31T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime issueEndAt,

    @NotNull(message = "사용 시작 일시는 필수입니다.")
    @Schema(description = "사용 가능 시작 일시", example = "2026-01-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime useStartAt,

    @NotNull(message = "사용 종료 일시는 필수입니다.")
    @Schema(description = "사용 가능 종료 일시", example = "2026-02-28T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime useEndAt,

    @Schema(description = "노출 여부 (미지정 시 미노출)", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    boolean visible
) {

    public CouponCreateCommand toCommand() {
        return new CouponCreateCommand(
            name(),
            description(),
            discountType(),
            discountAmount(),
            maxDiscountAmount(),
            minOrderAmount(),
            maxDiscountCount(),
            issueStartAt(),
            issueEndAt(),
            useStartAt(),
            useEndAt(),
            visible()
        );
    }
}

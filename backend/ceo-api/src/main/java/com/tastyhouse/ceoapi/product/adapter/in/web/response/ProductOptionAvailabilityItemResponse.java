package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityItemResult;

@Schema(description = "품절·숨김 관리 옵션 항목")
public record ProductOptionAvailabilityItemResponse(
    @Schema(description = "옵션 ID", example = "100")
    Long id,

    @Schema(description = "옵션 종류. 일괄 처리 요청 시 이 값을 함께 보낸다.", example = "NORMAL",
        allowableValues = {"NORMAL", "COMMON"})
    String optionType,

    @Schema(description = "옵션명", example = "곱빼기")
    String name,

    @Schema(description = "추가 금액", example = "1000")
    Integer additionalPrice,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut,

    @Schema(description = "품절 자동해제 시각. 무기한 품절이거나 판매중이면 null", example = "2026-08-18T09:00:00")
    LocalDateTime soldOutUntil,

    @Schema(description = "노출 여부", example = "true")
    boolean visible,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {
    public static ProductOptionAvailabilityItemResponse from(ProductOptionAvailabilityItemResult result) {
        return new ProductOptionAvailabilityItemResponse(
            result.id(),
            result.optionType(),
            result.name(),
            result.additionalPrice(),
            result.soldOut(),
            result.soldOutUntil(),
            result.visible(),
            result.sort()
        );
    }
}

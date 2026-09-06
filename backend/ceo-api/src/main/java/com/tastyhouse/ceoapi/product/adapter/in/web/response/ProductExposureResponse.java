package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.domain.product.model.ProductHiddenReason;
import com.tastyhouse.application.product.port.out.ProductExposureViewResult;

@Schema(description = "메뉴 노출기간 설정 현황")
public record ProductExposureResponse(
    @Schema(description = "노출 시작일. 하한이 없으면 null", example = "2026-05-01")
    LocalDate startDate,

    @Schema(description = "노출 종료일(당일 포함). 상한이 없으면 null", example = "2026-05-31")
    LocalDate endDate,

    @Schema(description = "요일·시간대 목록. 비어 있으면 요일·시간 제약이 없습니다.")
    List<ProductExposureHourResponse> hours,

    @Schema(description = "지금 손님 메뉴판에 노출 중인지", example = "true")
    boolean exposedNow,

    @Schema(description = "노출 중이 아닌 사유. 노출 중이면 null", example = "OUT_OF_EXPOSURE_HOURS",
        allowableValues = {"MANUALLY_HIDDEN", "BEFORE_EXPOSURE_PERIOD",
            "AFTER_EXPOSURE_PERIOD", "OUT_OF_EXPOSURE_HOURS"})
    String hiddenReason
) {
    public static ProductExposureResponse from(ProductExposureViewResult result) {
        ProductHiddenReason hiddenReason = result.hiddenReason();
        return new ProductExposureResponse(
            result.startDate(),
            result.endDate(),
            result.hours().stream()
                .map(ProductExposureHourResponse::from)
                .toList(),
            result.exposed(),
            hiddenReason == null ? null : hiddenReason.name()
        );
    }
}

package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 노출기간 설정 + 지금 노출 중인지의 판정 결과.
 *
 * <p>설정값만 내려주면 점주가 "설정했는데 왜 안 보이지"를 스스로 판단할 수 없으므로
 * {@code exposedNow}와 그 사유를 함께 담는다.
 *
 * <p>품절은 이 축에 없다 — 품절 메뉴는 목록에 남은 채 '품절' 뱃지만 붙으므로 노출 판정과 직교한다.
 */
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

    public static ProductExposureResponse from(
        LocalDate startDate,
        LocalDate endDate,
        List<ProductExposureHourResponse> hours,
        boolean exposedNow,
        String hiddenReason
    ) {
        return new ProductExposureResponse(
            startDate,
            endDate,
            hours,
            exposedNow,
            hiddenReason
        );
    }
}

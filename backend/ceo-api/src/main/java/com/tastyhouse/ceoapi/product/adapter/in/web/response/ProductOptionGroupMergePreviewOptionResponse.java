package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductOptionGroupMergePreviewResult;

/**
 * 미리보기의 옵션 1건.
 *
 * <p>{@code diffType}이 이 화면의 본질이다 — 합치기는 되돌릴 수 없으므로, 점주가 "무엇이 남고 무엇이
 * 사라지는지"를 수락한 상태에서만 실행돼야 한다.
 */
@Schema(description = "합치기 미리보기 옵션")
public record ProductOptionGroupMergePreviewOptionResponse(
    @Schema(description = "옵션 ID", example = "100")
    Long id,

    @Schema(description = "옵션명", example = "곱빼기")
    String name,

    @Schema(description = "추가 금액(원)", example = "1000")
    Integer additionalPrice,

    @Schema(description = "품절 여부", example = "false")
    Boolean soldOut,

    @Schema(description = "노출 여부", example = "true")
    Boolean visible,

    @Schema(description = "기준 그룹과의 차이. SAME=동일, ONLY_IN_BASE=기준에만 있음, "
        + "ONLY_IN_CANDIDATE=후보에만 있음(합치면 사라짐), PRICE_DIFFERS=이름은 같고 가격이 다름",
        example = "SAME",
        allowableValues = {"SAME", "ONLY_IN_BASE", "ONLY_IN_CANDIDATE", "PRICE_DIFFERS"})
    String diffType
) {

    public static ProductOptionGroupMergePreviewOptionResponse from(
        ProductOptionGroupMergePreviewResult.Option option
    ) {
        return new ProductOptionGroupMergePreviewOptionResponse(
            option.id(),
            option.name(),
            option.additionalPrice(),
            option.soldOut(),
            option.visible(),
            option.diffType()
        );
    }
}

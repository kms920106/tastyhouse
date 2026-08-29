package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 옵션그룹 관리 화면의 개별 옵션.
 *
 * <p>품절 여부를 담지 않는다 — 품절·숨김 조작은 별도 화면(품절·숨김 관리)의 관심사이고, 그 화면은
 * {@code ProductOptionAvailabilityItemResponse}를 쓴다.
 */
@Schema(description = "옵션")
public record ProductOptionResponse(
    @Schema(description = "옵션 ID", example = "5")
    Long id,

    @Schema(description = "옵션명", example = "아주 매운맛")
    String name,

    @Schema(description = "추가 금액(원). 무료 옵션은 0", example = "500")
    Integer additionalPrice,

    @Schema(description = "노출 순서(0부터). 순서 변경 시 서버가 0..N-1로 정규화한다.", example = "0")
    Integer sort,

    @Schema(description = "노출 여부. 삭제(감추기)한 옵션은 false다 — 이 목록은 감춘 옵션도 포함하므로 "
        + "화면이 이 값으로 걸러내거나 '삭제됨' 배지를 붙여야 한다.", example = "true")
    Boolean visible,

    @Schema(description = "일회용컵 제공 개수. 보증금 옵션그룹의 옵션만 값을 갖는다.", example = "1")
    Integer cupCount,

    @Schema(description = "보증금 금액(원). cupCount × 300으로 서버가 계산한 값이며 저장되지 않는 파생 "
        + "값이다 — 요율이 바뀌면 이 값도 함께 바뀐다(과거 주문 금액은 주문 스냅샷이 별도 보존).",
        example = "300")
    Integer depositAmount,

    @Schema(description = "개인컵 사용 할인 금액(원). 개인컵 옵션이 아니면 null이다.", example = "300")
    Integer personalCupDiscountAmount
) {

    public static ProductOptionResponse from(
        Long id,
        String name,
        Integer additionalPrice,
        Integer sort,
        Boolean visible,
        Integer cupCount,
        Integer depositAmount,
        Integer personalCupDiscountAmount
    ) {
        return new ProductOptionResponse(
            id,
            name,
            additionalPrice,
            sort,
            visible,
            cupCount,
            depositAmount,
            personalCupDiscountAmount
        );
    }
}

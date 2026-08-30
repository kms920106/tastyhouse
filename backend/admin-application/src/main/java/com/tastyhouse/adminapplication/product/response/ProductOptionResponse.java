package com.tastyhouse.adminapplication.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 옵션 응답")
public record ProductOptionResponse(
    @Schema(description = "옵션 ID", example = "10")
    Long id,

    @Schema(description = "옵션명", example = "매운맛")
    String name,

    @Schema(description = "추가 금액", example = "500")
    Integer additionalPrice,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut,

    @Schema(description = "일회용컵 제공 개수. 보증금 옵션그룹의 옵션만 값을 갖습니다.", example = "1")
    Integer cupCount,

    @Schema(description = "일회용컵 보증금(원). 컵 개수 × 300원으로 계산된 파생 값입니다.", example = "300")
    Integer depositAmount,

    @Schema(description = "개인컵 사용 할인 금액(원)", example = "300")
    Integer personalCupDiscountAmount
) {
    public static ProductOptionResponse from(
        Long id,
        String name,
        Integer additionalPrice,
        boolean soldOut,
        Integer cupCount,
        Integer depositAmount,
        Integer personalCupDiscountAmount
    ) {
        return new ProductOptionResponse(
            id,
            name,
            additionalPrice,
            soldOut,
            cupCount,
            depositAmount,
            personalCupDiscountAmount
        );
    }
}

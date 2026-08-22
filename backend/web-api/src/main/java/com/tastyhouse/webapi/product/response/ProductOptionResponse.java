package com.tastyhouse.webapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "옵션")
public record ProductOptionResponse(
    @Schema(description = "옵션 ID", example = "1")
    Long id,

    @Schema(description = "옵션명", example = "많이 맵게")
    String name,

    @Schema(description = "추가 금액", example = "0")
    Integer additionalPrice,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut,

    @Schema(description = "일회용컵 제공 개수. 보증금 옵션그룹의 옵션만 값을 갖는다.", example = "1")
    Integer cupCount,

    @Schema(description = "일회용컵 보증금(원). 컵 개수 × 300원. 이 금액은 최소주문금액·쿠폰·포인트 "
        + "산정에서 제외되며 결제 금액에는 별도 항목으로 가산된다. 반납 시 환급 대상이다.",
        example = "300")
    Integer depositAmount,

    @Schema(description = "개인컵 사용 할인 금액(원). 보증금이 아니라 상품 할인으로 반영된다.",
        example = "300")
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

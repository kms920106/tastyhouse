package com.tastyhouse.webapplication.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배치 조회 옵션")
public record ProductBatchOptionResponse(
    @Schema(description = "옵션 ID", example = "1")
    Long id,

    @Schema(description = "옵션명", example = "라지")
    String name,

    @Schema(description = "옵션 추가 금액", example = "3000")
    Integer price,

    @Schema(description = "일회용컵 제공 개수. 보증금 옵션그룹의 옵션만 값을 갖는다.", example = "1")
    Integer cupCount,

    @Schema(description = "일회용컵 보증금(원). 컵 개수 × 300원. 결제화면 보증금 합계의 원천이므로 "
        + "이 값이 빠지면 프론트 합계가 서버 계산값과 어긋나 주문이 거부된다.", example = "300")
    Integer depositAmount,

    @Schema(description = "개인컵 사용 할인 금액(원). 보증금이 아니라 상품 할인으로 반영된다.", example = "300")
    Integer personalCupDiscountAmount
) {
    public static ProductBatchOptionResponse from(
        Long id,
        String name,
        Integer price,
        Integer cupCount,
        Integer depositAmount,
        Integer personalCupDiscountAmount
    ) {
        return new ProductBatchOptionResponse(
            id,
            name,
            price,
            cupCount,
            depositAmount,
            personalCupDiscountAmount
        );
    }
}

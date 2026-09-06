package com.tastyhouse.adminapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.StorePriceVerificationItemResult;

@Schema(description = "매장 가격 인증 대상 메뉴 항목")
public record StorePriceVerificationItemResponse(
    @Schema(description = "메뉴 ID", example = "5")
    Long productId,

    @Schema(description = "메뉴명", example = "명란 크림 파스타")
    String productName,

    @Schema(description = "가격 행 ID", example = "31")
    Long priceId,

    @Schema(description = "가격명(단일 가격이면 null)", example = "라지")
    String priceName,

    @Schema(description = "점주가 신고한 매장 가격(승인 시 반영될 값)", example = "15000")
    Integer storePrice,

    @Schema(description = "현재 앱 노출 배달가(대조 기준)", example = "16500")
    Integer deliveryPrice,

    @Schema(description = "픽업가를 매장가와 동일하게 설정할지", example = "true")
    boolean applyPickupSamePrice
) {
    public static StorePriceVerificationItemResponse from(StorePriceVerificationItemResult result) {
        return new StorePriceVerificationItemResponse(
            result.productId(),
            result.productName(),
            result.priceId(),
            result.priceName(),
            result.storePrice(),
            result.deliveryPrice(),
            result.applyPickupSamePrice()
        );
    }
}

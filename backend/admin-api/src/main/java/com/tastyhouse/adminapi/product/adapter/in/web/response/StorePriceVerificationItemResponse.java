package com.tastyhouse.adminapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.StorePriceVerificationItemResult;

/**
 * 매장 가격 인증 요청의 대상 메뉴 항목 — 검수 대조표의 한 줄.
 *
 * <p>{@code deliveryPrice}(현재 앱 노출가)와 {@code storePrice}(점주가 신고한 매장가)를 <b>나란히</b>
 * 담는 것이 이 record의 존재 이유다. 검수자는 가격표 이미지의 금액이 {@code storePrice}와 맞는지,
 * 그리고 그 값이 앱 가격과 비교해 타당한지를 함께 본다.
 *
 * <p>{@code deliveryPrice}는 <b>조회 시점의 현재 값</b>이라 요청 접수 시점과 다를 수 있다. 반면
 * {@code storePrice}는 요청 시점에 박제된 값이며 승인 시 그대로 반영된다 — 검수자가 보지 않은 값이
 * 승인되지 않게 하려는 설계다.
 */
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

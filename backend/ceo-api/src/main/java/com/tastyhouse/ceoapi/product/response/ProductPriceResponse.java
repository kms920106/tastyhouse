package com.tastyhouse.ceoapi.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 가격 행 한 줄 응답.
 *
 * <p>{@code storePrice}·{@code pickupPrice}는 매장 가격 인증 전에는 {@code null}이다 — 빈 값을 0으로
 * 뭉개지 않는다. 0원은 "무료"라는 정당한 값이라, 미설정과 합치면 화면이 "매장가 0원"을 표시한다.
 *
 * <p>{@code id}를 함께 내려주는 것이 전체 교체(PUT)의 전제다. 화면은 이 {@code id}를 그대로 실어
 * 보내야 기존 행이 갱신되고, 빠뜨린 행은 삭제된다.
 */
@Schema(description = "메뉴 가격 행")
public record ProductPriceResponse(
    @Schema(description = "가격 행 ID", example = "10")
    Long id,

    @Schema(description = "가격명(가격 행이 1개면 null일 수 있습니다)", example = "대")
    String priceName,

    @Schema(description = "배달가격(원)", example = "15000")
    Integer deliveryPrice,

    @Schema(description = "매장가격(원). 미인증·미설정이면 null", example = "14000")
    Integer storePrice,

    @Schema(description = "픽업가격(원). 미인증·미설정이면 null", example = "14000")
    Integer pickupPrice,

    @Schema(description = "표시 순서(0부터)", example = "0")
    Integer sort
) {

    public static ProductPriceResponse from(
        Long id,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort
    ) {
        return new ProductPriceResponse(id, priceName, deliveryPrice, storePrice, pickupPrice, sort);
    }
}

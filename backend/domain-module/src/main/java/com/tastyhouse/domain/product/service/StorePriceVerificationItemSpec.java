package com.tastyhouse.domain.product.service;

/**
 * 매장 가격 인증 대상 항목 한 건의 <b>요청</b> — 인증 요청 서비스의 입력이다.
 *
 * <p>{@code storePrice}가 <b>요청 시점의 값으로 박제</b>되어 항목 행에 저장된다. 승인이 나중에
 * 이뤄지므로 그 사이 점주가 가격을 바꿔도 검수자가 본 가격 그대로 반영돼야 한다.
 *
 * <p>{@code applyPickupSamePrice}가 켜지면 승인 시 픽업가도 매장가와 같게 설정된다
 * (요구사항의 '픽업가격 동일 설정' 체크박스).
 */
public record StorePriceVerificationItemSpec(
    Long productId,
    Long priceId,
    Integer storePrice,
    boolean applyPickupSamePrice
) {

    public static StorePriceVerificationItemSpec of(
        Long productId,
        Long priceId,
        Integer storePrice,
        boolean applyPickupSamePrice
    ) {
        return new StorePriceVerificationItemSpec(productId, priceId, storePrice, applyPickupSamePrice);
    }
}

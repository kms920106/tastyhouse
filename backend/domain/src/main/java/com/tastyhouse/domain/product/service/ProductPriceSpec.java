package com.tastyhouse.domain.product.service;

/**
 * 가격 행 하나의 <b>요청</b> — 가격 전체 교체(PUT)의 입력이다.
 *
 * <p>{@code id}가 있으면 기존 행 갱신이고, {@code null}이면 신규 추가다. 요청에 담기지 않은 기존 행은
 * 삭제된다(전체 교체 의미론).
 *
 * <p>도메인 모델({@code ProductPrice})을 입력으로 받지 않는 이유는, 아직 검증되지 않은 클라이언트 값을
 * 도메인 객체로 만들면 불변식을 통과한 것과 통과하지 않은 것이 같은 타입으로 섞이기 때문이다
 * ({@code OrderLineSelection}과 같은 판단).
 *
 * <p>{@code storePrice}·{@code pickupPrice}는 인증 전에는 {@code null}이어야 한다 — 그 게이트는
 * {@code ProductPriceService}가 검증한다.
 */
public record ProductPriceSpec(
    Long id,
    String priceName,
    Integer deliveryPrice,
    Integer storePrice,
    Integer pickupPrice,
    Integer sort
) {

    public static ProductPriceSpec of(
        Long id,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort
    ) {
        return new ProductPriceSpec(id, priceName, deliveryPrice, storePrice, pickupPrice, sort);
    }
}

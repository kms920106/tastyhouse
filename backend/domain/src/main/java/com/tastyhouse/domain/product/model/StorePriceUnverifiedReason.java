package com.tastyhouse.domain.product.model;

/**
 * 메뉴가 매장가격 인증을 충족하지 못한 사유 — 점주에게 "왜 인증 OFF인지"를 알리는 값이다.
 *
 * <p>인증이 OFF인 것만 알려주면 점주는 무엇을 고쳐야 하는지 알 수 없다. 두 사유는 조치가 서로 다르다 —
 * 미등록은 매장가를 <b>입력</b>해야 하고, 배달가 초과는 <b>배달가를 내리거나 매장가를 올려야</b> 한다.
 *
 * <p>판정은 가격 행 자신이 수행한다({@code ProductPrice#resolveUnverifiedReason}) — 한 행의 값만으로
 * 답할 수 있는 술어이기 때문이다.
 */
public enum StorePriceUnverifiedReason {

    DELIVERY_PRICE_HIGHER_THAN_STORE("배달가격이 매장가격보다 높습니다."),
    STORE_PRICE_NOT_REGISTERED("등록된 매장가격이 없습니다.");

    private final String description;

    StorePriceUnverifiedReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}

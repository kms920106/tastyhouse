package com.tastyhouse.domain.product.model;

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

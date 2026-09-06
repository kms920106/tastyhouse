package com.tastyhouse.domain.shop.model;

public enum ShopChangeActorType {
    CEO("점주"),
    ADMIN("관리자");

    private final String description;

    ShopChangeActorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}

package com.tastyhouse.domain.shop.model;

public enum ShopOperatingStatus {
    OPEN("영업중"),
    PREPARING("준비중");

    private final String description;

    ShopOperatingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}

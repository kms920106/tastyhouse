package com.tastyhouse.domain.shop.model;

public enum ShopChangeActionType {
    CREATE("등록"),
    UPDATE("수정"),
    DELETE("삭제");

    private final String description;

    ShopChangeActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}

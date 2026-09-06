package com.tastyhouse.domain.shop.model;

public enum ShopRequestCommentAuthorType {
    CEO("점주"),
    ADMIN("담당자");

    private final String description;

    ShopRequestCommentAuthorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}

package com.tastyhouse.domain.shop.model;

/**
 * 가게 변경이력의 변경 주체.
 *
 * <p>1차 범위는 점주 변경({@code CEO})만 기록하지만, 관리자 조치를 후속으로 붙일 때 스키마를 바꾸지
 * 않도록 {@code ADMIN}을 미리 둔다.
 */
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

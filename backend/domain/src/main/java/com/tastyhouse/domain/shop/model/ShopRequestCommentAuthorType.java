package com.tastyhouse.domain.shop.model;

/**
 * 요청건 문의 스레드 작성자 유형.
 *
 * <p>{@code ShopChangeActorType}을 재사용하지 않는다 — 그쪽은 이름·Javadoc이 "가게 설정을 변경한 주체"로
 * 못박혀 있어 한쪽에서 값을 추가하면(예: {@code SYSTEM} 자동 안내) 다른 쪽 의미가 오염된다.
 */
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

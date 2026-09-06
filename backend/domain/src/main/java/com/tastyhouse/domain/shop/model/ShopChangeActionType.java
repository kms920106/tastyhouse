package com.tastyhouse.domain.shop.model;

/**
 * 가게 변경이력의 조치 유형.
 *
 * <p>컬렉션을 통째로 교체하는 replace-all 변경도 {@code UPDATE} 한 건으로 기록한다 —
 * 이력 1행의 단위가 "점주가 저장 버튼을 1번 누른 것"이기 때문이다.
 */
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

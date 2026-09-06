package com.tastyhouse.domain.shop.vo;

/**
 * 메뉴모음컷 식별자 VO.
 *
 * <p>다른 ID VO와 같은 형태로 양수만 허용한다 — 경계에서 {@code 0}·음수·{@code null}을 걸러내면
 * "존재하지 않는 식별자로 조회했는데 조용히 빈 결과가 나오는" 경로가 생기지 않는다.
 */
public record ShopMenuCollectionImageId(Long value) {

    public ShopMenuCollectionImageId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(" 양수여야 합니다: " + value);
        }
    }

    public static ShopMenuCollectionImageId of(Long value) {
        return new ShopMenuCollectionImageId(value);
    }
}

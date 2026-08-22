package com.tastyhouse.domain.shop.vo;

/**
 * 주문안내 식별자 VO.
 *
 * <p>주문안내는 가게당 1건이므로 {@code ShopId}만으로도 대상을 특정할 수 있지만, 관리자 게시중단
 * 경로({@code PATCH .../hide})가 행 자체를 다루고 변경이력이 행 단위로 남으므로 PK를 값 타입으로
 * 승격해 둔다 — raw {@code Long}을 그대로 흘리면 {@code shopId}와 {@code id}가 같은 타입이라
 * 인자 순서를 바꿔 넘겨도 컴파일이 통과한다.
 */
public record ShopOrderNoticeId(Long value) {

    public ShopOrderNoticeId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ShopOrderNoticeId는 양수여야 합니다: " + value);
        }
    }

    public static ShopOrderNoticeId of(Long value) {
        return new ShopOrderNoticeId(value);
    }
}

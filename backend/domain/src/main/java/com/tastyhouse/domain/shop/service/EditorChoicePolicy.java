package com.tastyhouse.domain.shop.service;

/**
 * 에디터 추천(초이스) 노출 정책.
 *
 * <p>추천 카드 하나에 함께 노출하는 상품 수는 화면 구성이 아니라 큐레이션 정책이므로 도메인이 소유한다
 * (선례: {@code reservation/domain/service/SlotPolicy}). 과거에는 이 상수가 조회 어댑터
 * {@code ShopChoiceQueryDao}에 리터럴로 있어 정책이 인프라에 살았다.
 */
public final class EditorChoicePolicy {

    /**
     * 에디터 추천 카드에 함께 노출하는 상품 수.
     */
    public static final int PRODUCT_LIMIT = 2;

    private EditorChoicePolicy() {
    }
}

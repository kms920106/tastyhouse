package com.tastyhouse.domain.product.service;

/**
 * 메뉴-가게 연결 한 건의 요청 명세(가게 + 그 가게에서의 메뉴그룹).
 *
 * <p>presentation의 Request 타입을 도메인 서비스가 직접 받지 않기 위한 경계 타입이다
 * ({@code ProductPriceSpec}과 같은 역할). {@code sort}는 담지 않는다 — 표시 순서는 요청이 지정하는
 * 값이 아니라 대상 가게의 기존 메뉴판 끝에 붙여 서버가 정하기 때문이다.
 */
public record ProductShopLinkSpec(Long shopId, Long productCategoryId) {

    public static ProductShopLinkSpec of(Long shopId, Long productCategoryId) {
        return new ProductShopLinkSpec(shopId, productCategoryId);
    }
}

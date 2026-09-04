package com.tastyhouse.application.shop.port.out;

/**
 * 가게 매장가격 뱃지 2종의 노출 여부.
 *
 * <p><b>챕터 10</b>에서 신설. 두 플래그는 <b>도메인 정책 {@code StorePriceBadgePolicy}의 판정 결과</b>이며,
 * 그 입력(가격 행·메뉴 수·지나간 영업일)을 모으는 과정에 read model → 도메인 모델 재구성과 영업일 산출이
 * 들어간다 — web-api가 할 수 없는 계산이므로 전부 서비스에 남고 판정 결과만 담는다.
 *
 * <p>판정 근거(매장가·픽업가·커버리지 비율)는 담지 않는다 — 손님 계약에 노출할 값이 아니다.
 */
public record ShopPriceBadgeViewResult(
    boolean sameAsStorePrice,
    boolean storePricePickup
) {
}

package com.tastyhouse.application.product.port.out;

/**
 * 메뉴 연결 화면의 가게 한 줄 — 점주가 소유한 가게와 이 메뉴의 연결 여부.
 *
 * <p><b>연결된 가게만이 아니라 소유한 전체 가게를 내려보낸다.</b> 화면이 토글로 켜고 끄는 형태이므로,
 * 연결되지 않은 가게도 목록에 있어야 켤 수 있다. {@code linked}가 그 상태를 표시한다.
 *
 * <p>{@code productCategoryId}·{@code productCategoryName}은 <b>연결된 가게만</b> 값을 갖는다 —
 * 연결되지 않은 가게에는 아직 배치된 메뉴그룹이 없기 때문이다. 화면은 토글을 켤 때 그 가게의
 * 메뉴그룹 목록에서 하나를 고르게 해야 한다(메뉴그룹 선택은 필수다).
 */
public record ProductShopLinkResult(
    Long shopId,
    String shopName,
    Long productCategoryId,
    String productCategoryName,
    boolean linked
) {
}

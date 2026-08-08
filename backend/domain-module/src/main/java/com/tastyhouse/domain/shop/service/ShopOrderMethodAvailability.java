package com.tastyhouse.domain.shop.service;

/**
 * 한 가게·한 주문유형에 대한 두 판정 결과 묶음.
 *
 * <p>주문 접수 게이트는 "가게가 닫혔는가"와 "그 주문유형만 막혔는가"를 <b>다른 사유로 구분해</b>
 * 거절해야 하므로 두 판정이 모두 필요하다. 둘은 같은 애그리거트 조회 한 번에서 함께 나오고 함께
 * 소비되므로, 따로 돌려주면 호출부가 두 값을 짝지어 들고 다녀야 한다
 * ({@code OrderPlacementService}의 {@code DeliveryTipResolution}과 같은 형태).
 *
 * @param shopWide    주문유형과 무관한 가게 전체 판정({@code orderMethod = null})
 * @param orderMethod 그 주문유형 기준 판정. 전체 대상 중지와 그 유형에 걸린 중지를 함께 본다
 */
public record ShopOrderMethodAvailability(
    ShopOperatingStatusResult shopWide,
    ShopOperatingStatusResult orderMethod
) {
}

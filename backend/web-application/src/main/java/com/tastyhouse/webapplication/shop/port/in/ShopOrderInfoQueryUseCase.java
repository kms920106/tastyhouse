package com.tastyhouse.webapplication.shop.port.in;

import java.util.List;

import com.tastyhouse.webapplication.shop.port.out.ScheduledOrderSlotsViewResult;
import com.tastyhouse.webapplication.shop.port.out.ShopDeliveryTipViewResult;
import com.tastyhouse.webapplication.shop.port.out.ShopOrderMethodItemResult;

/**
 * 가게 주문 조건 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 10</b> 이후 반환 타입이 읽기 계약이다. 배달팁은 주문 총액의 유일한 가산 항목이므로
 * 금액 산출은 전부 이 계층에 남고, 컨트롤러는 {@code ShopDeliveryTipResponse.from(...)}으로 옮기기만
 * 한다.
 */
public interface ShopOrderInfoQueryUseCase {

    ScheduledOrderSlotsViewResult getScheduledOrderSlots(Long shopId, String orderMethod);

    ShopDeliveryTipViewResult getShopDeliveryTip(Long shopId, Long memberId, Long deliveryAddressId, Integer orderAmount, String orderMethod);

    List<ShopOrderMethodItemResult> getShopOrderMethods(Long shopId);
}

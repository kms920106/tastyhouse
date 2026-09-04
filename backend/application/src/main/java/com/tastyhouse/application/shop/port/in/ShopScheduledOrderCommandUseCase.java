package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 가게 예약주문 설정 쓰기 인바운드 포트.
 */
@CeoApp
public interface ShopScheduledOrderCommandUseCase {

    void updateScheduledOrder(ShopScheduledOrderUpdateCommand command);
}

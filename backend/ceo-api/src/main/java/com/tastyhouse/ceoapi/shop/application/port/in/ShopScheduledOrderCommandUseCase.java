package com.tastyhouse.ceoapi.shop.application.port.in;

/**
 * 점주 가게 예약주문 설정 쓰기 인바운드 포트.
 */
public interface ShopScheduledOrderCommandUseCase {

    void updateScheduledOrder(ShopScheduledOrderUpdateCommand command);
}

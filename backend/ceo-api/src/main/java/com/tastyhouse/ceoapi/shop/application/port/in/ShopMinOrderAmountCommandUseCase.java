package com.tastyhouse.ceoapi.shop.application.port.in;

/**
 * 점주 가게 최소주문금액 쓰기 인바운드 포트.
 */
public interface ShopMinOrderAmountCommandUseCase {

    void updateMinOrderAmount(ShopMinOrderAmountUpdateCommand command);
}

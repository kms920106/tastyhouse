package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 가게 최소주문금액 쓰기 인바운드 포트.
 */
@CeoApp
public interface ShopMinOrderAmountCommandUseCase {

    void updateMinOrderAmount(ShopMinOrderAmountUpdateCommand command);
}

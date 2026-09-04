package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 가게 원산지 표시 쓰기 인바운드 포트.
 */
@CeoApp
public interface ShopOriginInfoCommandUseCase {

    void updateOriginInfo(ShopOriginInfoUpdateCommand command);
}

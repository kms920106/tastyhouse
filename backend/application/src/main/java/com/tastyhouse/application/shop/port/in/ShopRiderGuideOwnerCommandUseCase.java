package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 가게 라이더 안내(문구·픽업 위치) 쓰기 인바운드 포트.
 */
@CeoApp
public interface ShopRiderGuideOwnerCommandUseCase {

    void updateVisitGuide(ShopRiderVisitGuideUpdateCommand command);

    void updatePickupLocation(ShopRiderPickupLocationOwnerUpdateCommand command);

    void clearPickupLocation(ShopRiderPickupLocationClearCommand command);
}

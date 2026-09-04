package com.tastyhouse.ceoapplication.shop.port.in;

/**
 * 점주 가게 라이더 안내(문구·픽업 위치) 쓰기 인바운드 포트.
 */
public interface ShopRiderGuideCommandUseCase {

    void updateVisitGuide(ShopRiderVisitGuideUpdateCommand command);

    void updatePickupLocation(ShopRiderPickupLocationUpdateCommand command);

    void clearPickupLocation(ShopRiderPickupLocationClearCommand command);
}

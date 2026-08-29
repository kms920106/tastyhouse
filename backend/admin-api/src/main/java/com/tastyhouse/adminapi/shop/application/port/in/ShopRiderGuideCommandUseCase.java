package com.tastyhouse.adminapi.shop.application.port.in;

/** 라이더 안내 관리 쓰기 인바운드 포트(admin). */
public interface ShopRiderGuideCommandUseCase {

    void deleteVisitGuide(ShopRiderVisitGuideDeleteCommand command);

    Long requestRevision(ShopRiderVisitGuideRevisionCommand command);

    void updatePickupLocation(ShopRiderPickupLocationUpdateCommand command);
}

package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/** 라이더 안내 관리 쓰기 인바운드 포트(admin). */
@AdminApp
public interface ShopRiderGuideManagementCommandUseCase {

    void deleteVisitGuide(ShopRiderVisitGuideDeleteCommand command);

    Long requestRevision(ShopRiderVisitGuideRevisionCommand command);

    void updatePickupLocation(ShopRiderPickupLocationManagementUpdateCommand command);
}

package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

/** 배달권역 조정 신청 심사 쓰기 인바운드 포트(admin). */
@AdminApp
public interface ShopDeliveryAreaAdjustmentManagementCommandUseCase {

    void changeStatus(ShopDeliveryAreaAdjustmentStatusChangeCommand command);

    void rejectAdjustment(ShopDeliveryAreaAdjustmentRejectCommand command);
}

package com.tastyhouse.adminapplication.shop.port.in;

/** 배달권역 조정 신청 심사 쓰기 인바운드 포트(admin). */
public interface ShopDeliveryAreaAdjustmentCommandUseCase {

    void changeStatus(ShopDeliveryAreaAdjustmentStatusChangeCommand command);

    void rejectAdjustment(ShopDeliveryAreaAdjustmentRejectCommand command);
}

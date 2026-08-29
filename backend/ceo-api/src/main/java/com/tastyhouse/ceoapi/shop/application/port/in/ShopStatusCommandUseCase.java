package com.tastyhouse.ceoapi.shop.application.port.in;

/**
 * 점주 가게 노출 상태 쓰기 인바운드 포트.
 */
public interface ShopStatusCommandUseCase {

    void updateStatus(ShopStatusUpdateCommand command);
}

package com.tastyhouse.ceoapi.shop.application.port.in;

/**
 * 점주 가게 원산지 표시 쓰기 인바운드 포트.
 */
public interface ShopOriginInfoCommandUseCase {

    void updateOriginInfo(ShopOriginInfoUpdateCommand command);
}

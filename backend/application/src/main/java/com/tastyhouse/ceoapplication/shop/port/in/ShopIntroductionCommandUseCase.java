package com.tastyhouse.ceoapplication.shop.port.in;

/**
 * 점주 가게 소개 문구 쓰기 인바운드 포트.
 */
public interface ShopIntroductionCommandUseCase {

    void updateIntroduction(ShopIntroductionUpdateCommand command);
}

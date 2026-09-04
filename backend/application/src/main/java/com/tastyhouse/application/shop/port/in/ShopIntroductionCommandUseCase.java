package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 가게 소개 문구 쓰기 인바운드 포트.
 */
@CeoApp
public interface ShopIntroductionCommandUseCase {

    void updateIntroduction(ShopIntroductionUpdateCommand command);
}

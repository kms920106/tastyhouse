package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 가게 전화번호 쓰기 인바운드 포트.
 */
@CeoApp
public interface ShopPhoneNumberCommandUseCase {

    Long addPhoneNumber(ShopPhoneNumberCreateCommand command);

    void deletePhoneNumber(ShopPhoneNumberDeleteCommand command);

    void designatePrimary(ShopPhoneNumberPrimaryDesignateCommand command);
}

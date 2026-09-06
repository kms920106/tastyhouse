package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopPhoneNumberCommandUseCase {

    Long addPhoneNumber(ShopPhoneNumberCreateCommand command);

    void deletePhoneNumber(ShopPhoneNumberDeleteCommand command);

    void designatePrimary(ShopPhoneNumberPrimaryDesignateCommand command);
}

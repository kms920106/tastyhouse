package com.tastyhouse.application.shop.port.in;

public interface ShopPhoneNumberCommandUseCase {

    Long addPhoneNumber(ShopPhoneNumberCreateCommand command);

    void deletePhoneNumber(ShopPhoneNumberDeleteCommand command);

    void designatePrimary(ShopPhoneNumberPrimaryDesignateCommand command);
}

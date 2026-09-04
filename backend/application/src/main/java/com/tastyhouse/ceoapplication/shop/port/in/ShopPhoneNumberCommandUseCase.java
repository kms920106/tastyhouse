package com.tastyhouse.ceoapplication.shop.port.in;

/**
 * 점주 가게 전화번호 쓰기 인바운드 포트.
 */
public interface ShopPhoneNumberCommandUseCase {

    Long addPhoneNumber(ShopPhoneNumberCreateCommand command);

    void deletePhoneNumber(ShopPhoneNumberDeleteCommand command);

    void designatePrimary(ShopPhoneNumberPrimaryDesignateCommand command);
}

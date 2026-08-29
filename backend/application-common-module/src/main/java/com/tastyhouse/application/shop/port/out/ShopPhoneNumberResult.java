package com.tastyhouse.application.shop.port.out;

public record ShopPhoneNumberResult(
    Long id,
    Long shopId,
    String phoneNumber,
    boolean primary,
    boolean virtual
) {
}

package com.tastyhouse.infrastructure.shop.query;

public record ShopPhoneNumberResult(
    Long id,
    Long shopId,
    String phoneNumber,
    boolean primary,
    boolean virtual
) {

}

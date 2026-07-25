package com.tastyhouse.core.domain.shop.application.dto.result;

public record ShopPhoneNumberResult(
    Long id,
    Long shopId,
    String phoneNumber,
    boolean primary,
    boolean virtual
) {

    public static ShopPhoneNumberResult from(Long id, Long shopId, String phoneNumber, boolean primary, boolean virtual) {
        return new ShopPhoneNumberResult(id, shopId, phoneNumber, primary, virtual);
    }
}

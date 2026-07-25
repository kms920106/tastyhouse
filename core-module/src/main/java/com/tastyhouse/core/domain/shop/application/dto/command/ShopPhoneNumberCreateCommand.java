package com.tastyhouse.core.domain.shop.application.dto.command;

public record ShopPhoneNumberCreateCommand(
    Long shopId,
    String phoneNumber,
    boolean virtual
) {

    public static ShopPhoneNumberCreateCommand of(Long shopId, String phoneNumber, boolean virtual) {
        return new ShopPhoneNumberCreateCommand(shopId, phoneNumber, virtual);
    }
}

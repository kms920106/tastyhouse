package com.tastyhouse.core.domain.shop.application.dto.command;

import java.math.BigDecimal;

public record ShopCreateCommand(
    Long ceoId,
    Long stationId,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    Long thumbnailImageFileId
) {

    public static ShopCreateCommand of(
        Long ceoId,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId
    ) {
        return new ShopCreateCommand(
            ceoId,
            stationId,
            name,
            latitude,
            longitude,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId
        );
    }
}

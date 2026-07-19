package com.tastyhouse.core.domain.shop.application.dto.command;

import java.math.BigDecimal;

public record ShopUpdateCommand(
    Long stationId,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    Long thumbnailImageFileId
) {

    public static ShopUpdateCommand of(
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId
    ) {
        return new ShopUpdateCommand(
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

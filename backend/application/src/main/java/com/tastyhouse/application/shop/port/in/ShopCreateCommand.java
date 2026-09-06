package com.tastyhouse.application.shop.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopCreateCommand(
    Long adminId,
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
    public ShopCreateCommand {
        if (adminId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

package com.tastyhouse.application.shop.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopRiderPickupLocationOwnerUpdateCommand(
    Long ceoId,
    Long shopId,
    String roadAddress,
    String lotAddress,
    String detailAddress,
    BigDecimal latitude,
    BigDecimal longitude
) {
    public ShopRiderPickupLocationOwnerUpdateCommand {
        if (ceoId == null || shopId == null || roadAddress == null || latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

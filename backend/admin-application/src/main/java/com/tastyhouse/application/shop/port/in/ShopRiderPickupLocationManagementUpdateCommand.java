package com.tastyhouse.application.shop.port.in;

import java.math.BigDecimal;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopRiderPickupLocationManagementUpdateCommand(
    Long shopId,
    Long adminId,
    String roadAddress,
    String lotAddress,
    String detailAddress,
    BigDecimal latitude,
    BigDecimal longitude
) {

    public ShopRiderPickupLocationManagementUpdateCommand {
        if (shopId == null || adminId == null || roadAddress == null
            || latitude == null || longitude == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

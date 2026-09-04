package com.tastyhouse.adminapplication.shop.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 수정 command. */
public record ShopUpdateCommand(
    Long shopId,
    Long stationId,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    Long thumbnailImageFileId
) {
    public ShopUpdateCommand {
        if (shopId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

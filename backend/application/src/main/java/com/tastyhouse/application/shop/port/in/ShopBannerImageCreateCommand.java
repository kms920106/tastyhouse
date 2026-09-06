package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopBannerImageCreateCommand(
    Long shopId,
    Long imageFileId,
    Integer sort
) {
    public ShopBannerImageCreateCommand {
        if (shopId == null || imageFileId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

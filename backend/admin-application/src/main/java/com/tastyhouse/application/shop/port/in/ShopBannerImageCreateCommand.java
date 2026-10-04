package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBannerImageCreateCommand(
    Long shopId,
    Long imageFileId,
    Integer sort
) {

    public ShopBannerImageCreateCommand {
        if (shopId == null || imageFileId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

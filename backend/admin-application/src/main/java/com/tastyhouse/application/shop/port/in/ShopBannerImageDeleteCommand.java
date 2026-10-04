package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBannerImageDeleteCommand(
    Long bannerImageId
) {

    public ShopBannerImageDeleteCommand {
        if (bannerImageId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBannerImageDeleteCommand of(Long bannerImageId) {
        return new ShopBannerImageDeleteCommand(bannerImageId);
    }
}

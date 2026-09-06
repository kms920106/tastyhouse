package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopBannerImageDeleteCommand(
    Long bannerImageId
) {
    public ShopBannerImageDeleteCommand {
        if (bannerImageId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBannerImageDeleteCommand of(Long bannerImageId) {
        return new ShopBannerImageDeleteCommand(bannerImageId);
    }
}

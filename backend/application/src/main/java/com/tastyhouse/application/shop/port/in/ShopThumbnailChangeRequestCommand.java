package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopThumbnailChangeRequestCommand(
    Long ceoId,
    Long shopId
) {
    public ShopThumbnailChangeRequestCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopThumbnailChangeRequestCommand of(Long ceoId, Long shopId) {
        return new ShopThumbnailChangeRequestCommand(ceoId, shopId);
    }
}

package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopThumbnailChangeRequestCommand(
    Long ceoId,
    Long shopId
) {

    public ShopThumbnailChangeRequestCommand {
        if (ceoId == null || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopThumbnailChangeRequestCommand of(Long ceoId, Long shopId) {
        return new ShopThumbnailChangeRequestCommand(ceoId, shopId);
    }
}

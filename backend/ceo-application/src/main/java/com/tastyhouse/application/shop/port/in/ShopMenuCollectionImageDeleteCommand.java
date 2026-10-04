package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopMenuCollectionImageDeleteCommand(
    Long ceoId,
    Long shopId,
    Long imageId
) {

    public ShopMenuCollectionImageDeleteCommand {
        if (ceoId == null || shopId == null || imageId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopMenuCollectionImageDeleteCommand of(Long ceoId, Long shopId, Long imageId) {
        return new ShopMenuCollectionImageDeleteCommand(ceoId, shopId, imageId);
    }
}

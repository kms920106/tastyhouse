package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopMenuCollectionImageCreateCommand(
    Long ceoId,
    Long shopId
) {

    public ShopMenuCollectionImageCreateCommand {
        if (ceoId == null || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopMenuCollectionImageCreateCommand of(Long ceoId, Long shopId) {
        return new ShopMenuCollectionImageCreateCommand(ceoId, shopId);
    }
}

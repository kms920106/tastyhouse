package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductShopLinkReplaceCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    List<ProductShopLinkItemCommand> links
) {

    public ProductShopLinkReplaceCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || links == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

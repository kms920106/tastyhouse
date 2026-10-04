package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductPriceReplaceCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    List<ProductPriceItemCommand> prices
) {

    public ProductPriceReplaceCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || prices == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

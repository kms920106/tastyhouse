package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 채식 인증 해제 command.
 */
public record ProductVegetarianClearCommand(
    Long ceoId,
    Long shopId,
    Long productId
) {
    public ProductVegetarianClearCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductVegetarianClearCommand of(Long ceoId, Long shopId, Long productId) {
        return new ProductVegetarianClearCommand(ceoId, shopId, productId);
    }
}

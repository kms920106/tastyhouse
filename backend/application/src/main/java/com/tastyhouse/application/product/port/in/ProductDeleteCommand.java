package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductDeleteCommand(
    Long ceoId,
    Long shopId,
    List<Long> productIds
) {
    public ProductDeleteCommand {
        if (ceoId == null
            || shopId == null
            || productIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

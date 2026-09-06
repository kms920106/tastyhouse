package com.tastyhouse.application.product.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductSoldOutUntilChangeCommand(
    Long ceoId,
    Long shopId,
    List<Long> productIds,
    LocalDateTime soldOutUntil
) {
    public ProductSoldOutUntilChangeCommand {
        if (ceoId == null
            || shopId == null
            || productIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

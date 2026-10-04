package com.tastyhouse.application.product.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

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
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

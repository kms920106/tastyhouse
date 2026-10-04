package com.tastyhouse.application.product.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductOptionSoldOutUntilChangeCommand(
    Long ceoId,
    Long shopId,
    List<ProductOptionTargetCommand> options,
    LocalDateTime soldOutUntil
) {

    public ProductOptionSoldOutUntilChangeCommand {
        if (ceoId == null
            || shopId == null
            || options == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

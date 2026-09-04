package com.tastyhouse.application.product.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션 품절 처리 command.
 */
public record ProductOptionSoldOutCommand(
    Long ceoId,
    Long shopId,
    List<ProductOptionTargetCommand> options,
    LocalDateTime soldOutUntil
) {
    public ProductOptionSoldOutCommand {
        if (ceoId == null
            || shopId == null
            || options == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

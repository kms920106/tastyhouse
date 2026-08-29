package com.tastyhouse.ceoapi.product.application.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 옵션 품절 기간 변경 command.
 */
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
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

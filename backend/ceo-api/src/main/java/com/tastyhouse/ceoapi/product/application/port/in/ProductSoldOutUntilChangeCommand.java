package com.tastyhouse.ceoapi.product.application.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 품절 기간 변경 command.
 */
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

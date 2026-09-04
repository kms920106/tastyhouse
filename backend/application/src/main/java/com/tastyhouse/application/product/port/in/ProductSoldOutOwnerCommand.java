package com.tastyhouse.application.product.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 품절 처리 command.
 */
public record ProductSoldOutOwnerCommand(
    Long ceoId,
    Long shopId,
    List<Long> productIds,
    LocalDateTime soldOutUntil
) {
    public ProductSoldOutOwnerCommand {
        if (ceoId == null
            || shopId == null
            || productIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

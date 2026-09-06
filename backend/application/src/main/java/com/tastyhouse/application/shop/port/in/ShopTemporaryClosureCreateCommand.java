package com.tastyhouse.application.shop.port.in;

import java.time.LocalDate;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopTemporaryClosureCreateCommand(
    Long ceoId,
    Long shopId,
    LocalDate startDate,
    LocalDate endDate
) {
    public ShopTemporaryClosureCreateCommand {
        if (ceoId == null || shopId == null || startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

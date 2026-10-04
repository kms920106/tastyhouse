package com.tastyhouse.application.shop.port.in;

import java.time.LocalDate;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopTemporaryClosureCreateCommand(
    Long ceoId,
    Long shopId,
    LocalDate startDate,
    LocalDate endDate
) {

    public ShopTemporaryClosureCreateCommand {
        if (ceoId == null || shopId == null || startDate == null || endDate == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

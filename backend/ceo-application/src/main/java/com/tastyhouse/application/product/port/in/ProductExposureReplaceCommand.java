package com.tastyhouse.application.product.port.in;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductExposureReplaceCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    LocalDate startDate,
    LocalDate endDate,
    List<ProductExposureHourCommand> hours
) {

    public ProductExposureReplaceCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

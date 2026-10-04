package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductVegetarianApproveCommand(Long requestId) {

    public ProductVegetarianApproveCommand {
        if (requestId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ProductVegetarianApproveCommand of(Long requestId) {
        return new ProductVegetarianApproveCommand(requestId);
    }
}

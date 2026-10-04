package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopCeoAssignCommand(
    Long adminId,
    Long shopId,
    Long ceoId
) {

    public ShopCeoAssignCommand {
        if (adminId == null || shopId == null || ceoId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

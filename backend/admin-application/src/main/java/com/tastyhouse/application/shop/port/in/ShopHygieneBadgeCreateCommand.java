package com.tastyhouse.application.shop.port.in;

import java.time.LocalDate;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopHygieneBadgeCreateCommand(
    Long shopId,
    String badgeType,
    LocalDate certifiedDate,
    String lastInspectionMonth
) {

    public ShopHygieneBadgeCreateCommand {
        if (shopId == null || badgeType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

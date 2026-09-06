package com.tastyhouse.application.shop.port.in;

import java.time.LocalDate;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopHygieneBadgeCreateCommand(
    Long shopId,
    String badgeType,
    LocalDate certifiedDate,
    String lastInspectionMonth
) {
    public ShopHygieneBadgeCreateCommand {
        if (shopId == null || badgeType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopHygieneBadgeDeleteCommand(
    Long hygieneBadgeId
) {

    public ShopHygieneBadgeDeleteCommand {
        if (hygieneBadgeId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopHygieneBadgeDeleteCommand of(Long hygieneBadgeId) {
        return new ShopHygieneBadgeDeleteCommand(hygieneBadgeId);
    }}

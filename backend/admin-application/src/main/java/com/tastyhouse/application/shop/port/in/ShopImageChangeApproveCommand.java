package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopImageChangeApproveCommand(
    Long requestId
) {

    public ShopImageChangeApproveCommand {
        if (requestId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopImageChangeApproveCommand of(Long requestId) {
        return new ShopImageChangeApproveCommand(requestId);
    }}

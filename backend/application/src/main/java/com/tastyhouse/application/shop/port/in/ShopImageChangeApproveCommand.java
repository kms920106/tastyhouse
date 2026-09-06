package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopImageChangeApproveCommand(
    Long requestId
) {
    public ShopImageChangeApproveCommand {
        if (requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopImageChangeApproveCommand of(Long requestId) {
        return new ShopImageChangeApproveCommand(requestId);
    }}

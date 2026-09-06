package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopTemporaryClosureDeleteCommand(
    Long ceoId,
    Long temporaryClosureId
) {
    public ShopTemporaryClosureDeleteCommand {
        if (ceoId == null || temporaryClosureId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopTemporaryClosureDeleteCommand of(Long ceoId, Long temporaryClosureId) {
        return new ShopTemporaryClosureDeleteCommand(ceoId, temporaryClosureId);
    }
}

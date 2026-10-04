package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopTemporaryClosureDeleteCommand(
    Long ceoId,
    Long temporaryClosureId
) {

    public ShopTemporaryClosureDeleteCommand {
        if (ceoId == null || temporaryClosureId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopTemporaryClosureDeleteCommand of(Long ceoId, Long temporaryClosureId) {
        return new ShopTemporaryClosureDeleteCommand(ceoId, temporaryClosureId);
    }
}

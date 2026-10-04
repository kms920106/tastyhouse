package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopContentBoardOwnerDeleteCommand(
    Long ceoId,
    Long shopId,
    Long contentBoardId
) {

    public ShopContentBoardOwnerDeleteCommand {
        if (ceoId == null || shopId == null || contentBoardId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopContentBoardOwnerDeleteCommand of(Long ceoId, Long shopId, Long contentBoardId) {
        return new ShopContentBoardOwnerDeleteCommand(ceoId, shopId, contentBoardId);
    }
}

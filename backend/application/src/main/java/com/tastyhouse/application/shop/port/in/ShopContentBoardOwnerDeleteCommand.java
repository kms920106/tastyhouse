package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopContentBoardOwnerDeleteCommand(
    Long ceoId,
    Long shopId,
    Long contentBoardId
) {
    public ShopContentBoardOwnerDeleteCommand {
        if (ceoId == null || shopId == null || contentBoardId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopContentBoardOwnerDeleteCommand of(Long ceoId, Long shopId, Long contentBoardId) {
        return new ShopContentBoardOwnerDeleteCommand(ceoId, shopId, contentBoardId);
    }
}

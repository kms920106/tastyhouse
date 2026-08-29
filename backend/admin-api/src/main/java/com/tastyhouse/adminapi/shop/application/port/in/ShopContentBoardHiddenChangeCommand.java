package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 콘텐츠보드 숨김/노출 전환 command. */
public record ShopContentBoardHiddenChangeCommand(
    Long contentBoardId,
    Boolean hidden
) {
    public ShopContentBoardHiddenChangeCommand {
        if (contentBoardId == null || hidden == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 테하 초이스 등록 command. */
public record ShopChoiceCreateCommand(
    Long shopId,
    String title,
    String content
) {
    public ShopChoiceCreateCommand {
        if (shopId == null || title == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

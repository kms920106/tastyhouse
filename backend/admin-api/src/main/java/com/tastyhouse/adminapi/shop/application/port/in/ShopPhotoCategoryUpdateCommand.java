package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 포토 카테고리 수정 command. */
public record ShopPhotoCategoryUpdateCommand(
    Long categoryId,
    String name
) {
    public ShopPhotoCategoryUpdateCommand {
        if (categoryId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

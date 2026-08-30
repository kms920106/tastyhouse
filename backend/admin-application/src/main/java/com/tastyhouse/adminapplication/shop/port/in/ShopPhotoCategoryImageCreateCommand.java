package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 포토 카테고리 이미지 등록 command. */
public record ShopPhotoCategoryImageCreateCommand(
    Long categoryId,
    Long imageFileId,
    Integer sort,
    Boolean visible
) {
    public ShopPhotoCategoryImageCreateCommand {
        if (categoryId == null || imageFileId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

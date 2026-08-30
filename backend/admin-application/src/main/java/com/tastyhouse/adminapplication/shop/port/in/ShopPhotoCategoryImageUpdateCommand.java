package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 포토 카테고리 이미지 수정 command. */
public record ShopPhotoCategoryImageUpdateCommand(
    Long imageId,
    Long imageFileId,
    Integer sort,
    Boolean visible
) {
    public ShopPhotoCategoryImageUpdateCommand {
        if (imageId == null || imageFileId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

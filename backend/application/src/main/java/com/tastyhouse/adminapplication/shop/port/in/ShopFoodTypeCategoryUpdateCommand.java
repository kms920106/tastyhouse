package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 음식종류 마스터 카테고리 수정 command. */
public record ShopFoodTypeCategoryUpdateCommand(
    Long categoryId,
    String displayName,
    Long activeImageFileId,
    Long inactiveImageFileId,
    Integer sort,
    Boolean visible
) {
    public ShopFoodTypeCategoryUpdateCommand {
        if (categoryId == null || displayName == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

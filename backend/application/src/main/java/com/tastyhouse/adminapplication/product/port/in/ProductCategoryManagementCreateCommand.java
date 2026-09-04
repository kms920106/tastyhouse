package com.tastyhouse.adminapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 상품 카테고리 등록 command. */
public record ProductCategoryManagementCreateCommand(
    Long shopId,
    String name,
    Integer sort,
    Boolean visible
) {
    public ProductCategoryManagementCreateCommand {
        if (shopId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

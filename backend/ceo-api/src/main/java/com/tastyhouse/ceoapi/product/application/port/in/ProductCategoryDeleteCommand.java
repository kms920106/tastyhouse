package com.tastyhouse.ceoapi.product.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 분류 삭제 command.
 */
public record ProductCategoryDeleteCommand(
    Long ceoId,
    Long productCategoryId,
    Long shopId
) {
    public ProductCategoryDeleteCommand {
        if (ceoId == null
            || productCategoryId == null
            || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

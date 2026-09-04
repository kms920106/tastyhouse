package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 분류 등록 command.
 */
public record ProductCategoryOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    String name,
    String description
) {
    public ProductCategoryOwnerCreateCommand {
        if (ceoId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 분류 등록 command.
 */
public record ProductCategoryCreateCommand(
    Long ceoId,
    Long shopId,
    String name,
    String description
) {
    public ProductCategoryCreateCommand {
        if (ceoId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 분류 이동 command.
 */
public record ProductRelocateCommand(
    Long ceoId,
    Long shopId,
    Long targetProductCategoryId,
    List<Long> productIds,
    List<Long> targetOrderedProductIds
) {
    public ProductRelocateCommand {
        if (ceoId == null
            || shopId == null
            || targetProductCategoryId == null
            || productIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

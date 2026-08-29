package com.tastyhouse.ceoapi.product.application.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 분류 내 메뉴 정렬 변경 command.
 */
public record ProductReorderCommand(
    Long ceoId,
    Long shopId,
    Long productCategoryId,
    List<Long> productIds
) {
    public ProductReorderCommand {
        if (ceoId == null
            || shopId == null
            || productCategoryId == null
            || productIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

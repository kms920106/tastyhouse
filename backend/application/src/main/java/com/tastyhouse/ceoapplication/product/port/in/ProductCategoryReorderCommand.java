package com.tastyhouse.ceoapplication.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 분류 정렬 변경 command.
 */
public record ProductCategoryReorderCommand(
    Long ceoId,
    Long shopId,
    List<Long> productCategoryIds
) {
    public ProductCategoryReorderCommand {
        if (ceoId == null
            || shopId == null
            || productCategoryIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

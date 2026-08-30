package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 영양정보 삭제 command.
 */
public record ProductNutritionDeleteCommand(
    Long ceoId,
    Long shopId,
    Long productId
) {
    public ProductNutritionDeleteCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductNutritionDeleteCommand of(Long ceoId, Long shopId, Long productId) {
        return new ProductNutritionDeleteCommand(ceoId, shopId, productId);
    }
}

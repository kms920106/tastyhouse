package com.tastyhouse.ceoapi.product.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 이미지 삭제 command.
 */
public record ProductImageDeleteCommand(
    Long ceoId,
    Long shopId,
    Long imageId
) {
    public ProductImageDeleteCommand {
        if (ceoId == null
            || shopId == null
            || imageId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductImageDeleteCommand of(Long ceoId, Long shopId, Long imageId) {
        return new ProductImageDeleteCommand(ceoId, shopId, imageId);
    }
}

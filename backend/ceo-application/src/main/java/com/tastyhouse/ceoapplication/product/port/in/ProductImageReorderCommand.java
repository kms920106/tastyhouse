package com.tastyhouse.ceoapplication.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 이미지 정렬 변경 command.
 */
public record ProductImageReorderCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    List<Long> imageIds
) {
    public ProductImageReorderCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || imageIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

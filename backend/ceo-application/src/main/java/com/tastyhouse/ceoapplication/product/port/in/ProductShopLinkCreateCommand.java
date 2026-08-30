package com.tastyhouse.ceoapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴를 다른 가게로 불러오는 연결 등록 command.
 */
public record ProductShopLinkCreateCommand(
    Long ceoId,
    Long productId,
    Long targetShopId,
    Long productCategoryId
) {
    public ProductShopLinkCreateCommand {
        if (ceoId == null
            || productId == null
            || targetShopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴-가게 연결 항목 command. 중첩 항목이라 Request record가 서비스로 누수되던 자리를 대신한다.
 */
public record ProductShopLinkItemCommand(
    Long shopId,
    Long productCategoryId
) {
    public ProductShopLinkItemCommand {
        if (shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

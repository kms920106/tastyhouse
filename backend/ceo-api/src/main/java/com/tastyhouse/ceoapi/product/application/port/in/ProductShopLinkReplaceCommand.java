package com.tastyhouse.ceoapi.product.application.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴-가게 연결 일괄 교체 command.
 */
public record ProductShopLinkReplaceCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    List<ProductShopLinkItemCommand> links
) {
    public ProductShopLinkReplaceCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || links == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

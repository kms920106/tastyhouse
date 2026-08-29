package com.tastyhouse.ceoapi.product.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴-가게 연결 해제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ProductShopLinkDeleteCommand(
    Long ceoId,
    Long productId,
    Long targetShopId
) {
    public ProductShopLinkDeleteCommand {
        if (ceoId == null
            || productId == null
            || targetShopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductShopLinkDeleteCommand of(Long ceoId, Long productId, Long targetShopId) {
        return new ProductShopLinkDeleteCommand(ceoId, productId, targetShopId);
    }
}

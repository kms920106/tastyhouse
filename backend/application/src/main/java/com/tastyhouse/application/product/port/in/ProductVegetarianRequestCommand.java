package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 채식 인증 요청 command. {@code vegetarianType}은 경계 타입인 문자열로 받고 enum 승격은 서비스가 한다.
 */
public record ProductVegetarianRequestCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    String vegetarianType,
    String ingredients,
    String description
) {
    public ProductVegetarianRequestCommand {
        if (ceoId == null
            || shopId == null
            || productId == null
            || vegetarianType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

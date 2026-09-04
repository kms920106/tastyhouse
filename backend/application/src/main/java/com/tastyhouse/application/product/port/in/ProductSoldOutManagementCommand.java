package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 상품 품절 처리 command. 요청 본문이 없는 상태 전이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ProductSoldOutManagementCommand(Long productId) {
    public ProductSoldOutManagementCommand {
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductSoldOutManagementCommand of(Long productId) {
        return new ProductSoldOutManagementCommand(productId);
    }
}

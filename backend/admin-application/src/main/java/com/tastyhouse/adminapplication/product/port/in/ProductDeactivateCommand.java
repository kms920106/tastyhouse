package com.tastyhouse.adminapplication.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 상품 비활성화 command. 요청 본문이 없는 상태 전이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ProductDeactivateCommand(Long productId) {
    public ProductDeactivateCommand {
        if (productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductDeactivateCommand of(Long productId) {
        return new ProductDeactivateCommand(productId);
    }
}

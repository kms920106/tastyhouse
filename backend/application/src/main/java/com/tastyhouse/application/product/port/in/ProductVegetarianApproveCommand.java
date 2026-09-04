package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 메뉴 채식 설정 요청 승인 command. 요청 본문이 없는 상태 전이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ProductVegetarianApproveCommand(Long requestId) {
    public ProductVegetarianApproveCommand {
        if (requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductVegetarianApproveCommand of(Long requestId) {
        return new ProductVegetarianApproveCommand(requestId);
    }
}

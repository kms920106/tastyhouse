package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 사장님 추천 지정 요청 승인 command. 요청 본문이 없는 상태 전이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ProductRepresentativeApproveCommand(Long requestId) {
    public ProductRepresentativeApproveCommand {
        if (requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ProductRepresentativeApproveCommand of(Long requestId) {
        return new ProductRepresentativeApproveCommand(requestId);
    }
}

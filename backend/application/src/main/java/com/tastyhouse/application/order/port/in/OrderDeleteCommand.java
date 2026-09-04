package com.tastyhouse.application.order.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 주문 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record OrderDeleteCommand(Long orderId) {
    public OrderDeleteCommand {
        if (orderId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static OrderDeleteCommand of(Long orderId) {
        return new OrderDeleteCommand(orderId);
    }
}

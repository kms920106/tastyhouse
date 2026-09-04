package com.tastyhouse.adminapplication.point.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 포인트 수동 적립 command. 경로 변수 {@code memberId}는 컨트롤러가 {@code toCommand(memberId)}로 주입한다.
 */
public record PointEarnCommand(
    Long memberId,
    Integer amount,
    String reason
) {
    public PointEarnCommand {
        if (memberId == null || amount == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

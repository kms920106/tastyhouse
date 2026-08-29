package com.tastyhouse.webapi.reservation.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 회원 본인 예약 취소 command. 요청 본문이 없는 상태전이라 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ReservationCancelCommand(
    Long memberId,
    Long reservationId
) {
    public ReservationCancelCommand {
        if (memberId == null || reservationId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReservationCancelCommand of(Long memberId, Long reservationId) {
        return new ReservationCancelCommand(memberId, reservationId);
    }
}

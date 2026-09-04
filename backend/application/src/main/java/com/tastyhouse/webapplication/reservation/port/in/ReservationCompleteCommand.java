package com.tastyhouse.webapplication.reservation.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 예약 complete command. 요청 본문이 없는 상태전이라 컨트롤러가 정적 팩토리로 조립한다.
 *
 * <p>TODO(보안): Shop-owner 연결 후 점주 식별자를 이 command에 추가해 본인 검증을 붙인다.
 */
public record ReservationCompleteCommand(Long reservationId) {
    public ReservationCompleteCommand {
        if (reservationId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReservationCompleteCommand of(Long reservationId) {
        return new ReservationCompleteCommand(reservationId);
    }
}

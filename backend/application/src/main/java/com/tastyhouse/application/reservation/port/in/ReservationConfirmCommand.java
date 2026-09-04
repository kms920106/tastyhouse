package com.tastyhouse.application.reservation.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 예약 confirm command. 요청 본문이 없는 상태전이라 컨트롤러가 정적 팩토리로 조립한다.
 *
 * <p>TODO(보안): Shop-owner 연결 후 점주 식별자를 이 command에 추가해 본인 검증을 붙인다.
 */
public record ReservationConfirmCommand(Long reservationId) {
    public ReservationConfirmCommand {
        if (reservationId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReservationConfirmCommand of(Long reservationId) {
        return new ReservationConfirmCommand(reservationId);
    }
}

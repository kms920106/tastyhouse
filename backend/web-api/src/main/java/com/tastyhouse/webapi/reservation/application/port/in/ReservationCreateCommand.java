package com.tastyhouse.webapi.reservation.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 예약 생성 command.
 *
 * <p>날짜 미래 여부·인원수 범위·약관 동의 검증은 Request의 jakarta.validation이 담당하고, 이 record는
 * 필수값 누락 같은 구조적 가드만 둔다.
 *
 * <p>{@code request}(요청사항)는 선택값이라 null을 허용한다.
 */
public record ReservationCreateCommand(
    Long memberId,
    Long shopId,
    LocalDate reservationDate,
    LocalTime reservationTime,
    Integer partySize,
    String request,
    Boolean agreedRequiredTerms
) {
    public ReservationCreateCommand {
        if (memberId == null || shopId == null || reservationDate == null
            || reservationTime == null || partySize == null || agreedRequiredTerms == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

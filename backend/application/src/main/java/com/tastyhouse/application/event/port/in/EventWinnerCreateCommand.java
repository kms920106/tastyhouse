package com.tastyhouse.application.event.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 당첨자 등록 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 *
 * <p>{@code winnerName}·{@code phoneNumber}가 연속된 {@code String}이라 순서가 뒤바뀌어도 컴파일된다.
 * 조립은 반드시 이름 있는 접근자로 한다.
 */
public record EventWinnerCreateCommand(
    Long eventId,
    Integer rankNo,
    String winnerName,
    String phoneNumber,
    LocalDateTime announcedAt
) {
    public EventWinnerCreateCommand {
        if (eventId == null || rankNo == null || winnerName == null || phoneNumber == null || announcedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

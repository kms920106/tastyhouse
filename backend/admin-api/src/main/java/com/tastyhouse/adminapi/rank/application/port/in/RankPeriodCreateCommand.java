package com.tastyhouse.adminapi.rank.application.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 랭킹 기간 등록 command.
 *
 * <p>{@code startAt}·{@code endAt}이 연속된 {@code LocalDateTime}이라 순서가 뒤바뀌어도 컴파일된다.
 * 조립은 반드시 이름 있는 접근자로 한다.
 */
public record RankPeriodCreateCommand(
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean visible
) {
    public RankPeriodCreateCommand {
        if (startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

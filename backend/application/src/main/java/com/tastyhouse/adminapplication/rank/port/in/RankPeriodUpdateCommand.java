package com.tastyhouse.adminapplication.rank.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 랭킹 기간 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 *
 * <p>{@code startAt}·{@code endAt}이 연속된 {@code LocalDateTime}이라 순서가 뒤바뀌어도 컴파일된다.
 * 조립은 반드시 이름 있는 접근자로 한다.
 */
public record RankPeriodUpdateCommand(
    Long rankPeriodId,
    LocalDateTime startAt,
    LocalDateTime endAt,
    boolean visible
) {
    public RankPeriodUpdateCommand {
        if (rankPeriodId == null || startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

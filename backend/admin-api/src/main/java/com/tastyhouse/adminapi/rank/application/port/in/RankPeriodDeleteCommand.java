package com.tastyhouse.adminapi.rank.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 랭킹 기간 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record RankPeriodDeleteCommand(Long rankPeriodId) {
    public RankPeriodDeleteCommand {
        if (rankPeriodId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static RankPeriodDeleteCommand of(Long rankPeriodId) {
        return new RankPeriodDeleteCommand(rankPeriodId);
    }
}

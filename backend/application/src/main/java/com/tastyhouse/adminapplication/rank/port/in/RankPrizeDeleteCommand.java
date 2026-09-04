package com.tastyhouse.adminapplication.rank.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 랭킹 경품 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record RankPrizeDeleteCommand(Long rankPrizeId) {
    public RankPrizeDeleteCommand {
        if (rankPrizeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static RankPrizeDeleteCommand of(Long rankPrizeId) {
        return new RankPrizeDeleteCommand(rankPrizeId);
    }
}

package com.tastyhouse.adminapplication.rank.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 랭킹 경품 수정 command. 경로 변수 {@code prizeId}는 컨트롤러가 {@code toCommand(prizeId)}로 주입한다.
 *
 * <p>{@code rankPrizeId}·{@code imageFileId}가 둘 다 {@code Long}, {@code name}·{@code brand}가 둘 다
 * {@code String}이라 순서가 뒤바뀌어도 컴파일된다. 조립은 반드시 이름 있는 접근자로 한다.
 */
public record RankPrizeUpdateCommand(
    Long rankPrizeId,
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId
) {
    public RankPrizeUpdateCommand {
        if (rankPrizeId == null || prizeRank == null || name == null || brand == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

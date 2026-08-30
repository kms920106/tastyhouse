package com.tastyhouse.adminapplication.rank.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 랭킹 경품 등록 command. 경로 변수 {@code id}(대상 기간)는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 *
 * <p>{@code rankPeriodId}·{@code imageFileId}가 둘 다 {@code Long}, {@code name}·{@code brand}가 둘 다
 * {@code String}이라 순서가 뒤바뀌어도 컴파일된다. 조립은 반드시 이름 있는 접근자로 한다.
 * {@code imageFileId}는 미지정 허용값이라 null을 받는다.
 */
public record RankPrizeCreateCommand(
    Long rankPeriodId,
    Integer prizeRank,
    String name,
    String brand,
    Long imageFileId
) {
    public RankPrizeCreateCommand {
        if (rankPeriodId == null || prizeRank == null || name == null || brand == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

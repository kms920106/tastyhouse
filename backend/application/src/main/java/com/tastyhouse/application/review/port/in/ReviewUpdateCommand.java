package com.tastyhouse.application.review.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 리뷰 수정 command.
 *
 * <p><b>{@code tasteRating}·{@code amountRating}·{@code priceRating} 세 {@code Integer}가 연달아
 * 있다</b> — 등록 command와 같은 위치 기반 뒤바뀜 위험이 있으므로 {@code toCommand}는 이름 기반
 * 접근자로 각 값을 짚어 넘긴다.
 *
 * <p>{@code ownerOnly}는 없다 — 사장님만보기는 등록 시에만 정할 수 있고 이후 전환이 불가능하다.
 */
public record ReviewUpdateCommand(
    Long memberId,
    Long reviewId,
    Integer tasteRating,
    Integer amountRating,
    Integer priceRating,
    String content,
    List<Long> uploadedFileIds,
    List<String> tags,
    Integer deliveryRating,
    String deliveryComment
) {
    public ReviewUpdateCommand {
        if (memberId == null || reviewId == null || tasteRating == null
            || amountRating == null || priceRating == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

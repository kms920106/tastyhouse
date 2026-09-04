package com.tastyhouse.application.review.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 리뷰 게시중단 요청 등록 command.
 *
 * <p>{@code reason}은 경계 타입인 문자열로 받고 enum 승격은 서비스가 수행한다. {@code reason=ETC}일 때
 * {@code detailReason}이 필수라는 조건부 규칙은 도메인 서비스가 판정하므로 여기서 가드하지 않는다.
 */
public record ReviewBlindRequestCreateCommand(
    Long ceoId,
    Long shopId,
    Long reviewId,
    String reason,
    String detailReason,
    List<Long> attachmentFileIds
) {
    public ReviewBlindRequestCreateCommand {
        if (ceoId == null || shopId == null || reviewId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

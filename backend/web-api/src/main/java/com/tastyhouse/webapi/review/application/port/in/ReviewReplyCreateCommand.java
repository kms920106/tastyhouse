package com.tastyhouse.webapi.review.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 댓글 답글 등록 command.
 *
 * <p><b>{@code memberId}·{@code commentId}·{@code replyToMemberId} 세 {@code Long}이 연달아 있다</b> —
 * 위치 기반으로 옮기면 작성자와 답글 대상이 조용히 뒤바뀌므로 {@code toCommand}는 이름 기반 접근자로
 * 각 값을 짚어 넘긴다.
 *
 * <p>{@code replyToMemberId}는 답글 대상이 없는 경우가 정상이라 null을 허용한다.
 */
public record ReviewReplyCreateCommand(
    Long memberId,
    Long commentId,
    Long replyToMemberId,
    String content
) {
    public ReviewReplyCreateCommand {
        if (memberId == null || commentId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

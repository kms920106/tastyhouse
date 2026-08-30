package com.tastyhouse.webapplication.follow.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 팔로워 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record FollowerRemoveCommand(
    Long memberId,
    Long followerId
) {
    public FollowerRemoveCommand {
        if (memberId == null || followerId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static FollowerRemoveCommand of(Long memberId, Long followerId) {
        return new FollowerRemoveCommand(memberId, followerId);
    }
}

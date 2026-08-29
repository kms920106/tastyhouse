package com.tastyhouse.webapi.follow.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 팔로우 등록 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record FollowCreateCommand(
    Long followerId,
    Long followingId
) {
    public FollowCreateCommand {
        if (followerId == null || followingId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static FollowCreateCommand of(Long followerId, Long followingId) {
        return new FollowCreateCommand(followerId, followingId);
    }
}

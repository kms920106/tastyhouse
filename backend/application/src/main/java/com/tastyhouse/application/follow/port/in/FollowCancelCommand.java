package com.tastyhouse.application.follow.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record FollowCancelCommand(
    Long followerId,
    Long followingId
) {
    public FollowCancelCommand {
        if (followerId == null || followingId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static FollowCancelCommand of(Long followerId, Long followingId) {
        return new FollowCancelCommand(followerId, followingId);
    }
}

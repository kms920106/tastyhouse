package com.tastyhouse.application.follow.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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

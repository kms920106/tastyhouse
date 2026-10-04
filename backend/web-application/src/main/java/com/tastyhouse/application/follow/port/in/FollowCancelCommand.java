package com.tastyhouse.application.follow.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record FollowCancelCommand(
    Long followerId,
    Long followingId
) {

    public FollowCancelCommand {
        if (followerId == null || followingId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static FollowCancelCommand of(Long followerId, Long followingId) {
        return new FollowCancelCommand(followerId, followingId);
    }
}

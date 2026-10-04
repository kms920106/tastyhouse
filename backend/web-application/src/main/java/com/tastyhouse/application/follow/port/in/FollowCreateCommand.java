package com.tastyhouse.application.follow.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record FollowCreateCommand(
    Long followerId,
    Long followingId
) {

    public FollowCreateCommand {
        if (followerId == null || followingId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static FollowCreateCommand of(Long followerId, Long followingId) {
        return new FollowCreateCommand(followerId, followingId);
    }
}

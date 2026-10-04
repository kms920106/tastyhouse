package com.tastyhouse.application.follow.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record FollowerRemoveCommand(
    Long memberId,
    Long followerId
) {

    public FollowerRemoveCommand {
        if (memberId == null || followerId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static FollowerRemoveCommand of(Long memberId, Long followerId) {
        return new FollowerRemoveCommand(memberId, followerId);
    }
}

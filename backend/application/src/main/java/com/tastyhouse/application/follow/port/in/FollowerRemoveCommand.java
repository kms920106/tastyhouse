package com.tastyhouse.application.follow.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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

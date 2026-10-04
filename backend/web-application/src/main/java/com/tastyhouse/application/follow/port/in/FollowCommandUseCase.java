package com.tastyhouse.application.follow.port.in;

public interface FollowCommandUseCase {

    Long follow(FollowCreateCommand command);

    void unfollow(FollowCancelCommand command);

    void removeFollower(FollowerRemoveCommand command);
}

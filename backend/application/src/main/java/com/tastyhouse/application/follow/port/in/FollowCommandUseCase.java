package com.tastyhouse.application.follow.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface FollowCommandUseCase {

    Long follow(FollowCreateCommand command);

    void unfollow(FollowCancelCommand command);

    void removeFollower(FollowerRemoveCommand command);
}

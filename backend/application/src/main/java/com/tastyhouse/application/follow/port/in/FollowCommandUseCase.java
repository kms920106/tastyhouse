package com.tastyhouse.application.follow.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

/**
 * 팔로우 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code FollowCommandService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
@WebApp
public interface FollowCommandUseCase {

    /**
     * @return 생성된 팔로우 관계 식별자
     */
    Long follow(FollowCreateCommand command);

    void unfollow(FollowCancelCommand command);

    void removeFollower(FollowerRemoveCommand command);
}

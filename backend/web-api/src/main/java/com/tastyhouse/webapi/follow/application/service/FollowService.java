package com.tastyhouse.webapi.follow.application.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.webapi.follow.application.port.in.FollowCancelCommand;
import com.tastyhouse.webapi.follow.application.port.in.FollowCommandUseCase;
import com.tastyhouse.webapi.follow.application.port.in.FollowCreateCommand;
import com.tastyhouse.webapi.follow.application.port.in.FollowerRemoveCommand;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.follow.adapter.in.web.response.FollowMemberListItemResponse;
import com.tastyhouse.webapi.follow.adapter.in.web.response.FollowMemberSearchListItemResponse;

/**
 * 팔로우 컨트롤러 파사드.
 *
 * <p>CQRS 분리에 따라 실제 처리는 {@link FollowCommandUseCase}/{@link FollowQueryService}가 담당하고,
 * 이 클래스는 컨트롤러가 쓰는 진입점(공개/로그인 목록 구분 등)만 얇게 유지한다.
 *
 * <p><b>트랜잭션 원자성 판정: 묶을 대상 없음(파사드에 {@code @Transactional} 불필요).</b> 이 파사드의 모든
 * 메서드는 하위 CommandService/QueryService <em>한 곳</em>에만 위임하는 1:1 pass-through이며, 한 유스케이스가
 * 여러 하위 서비스를 순서대로 엮는 지점이 없다. 따라서 "여러 트랜잭션으로 쪼개진 검증-갱신 시퀀스"가
 * 애초에 존재하지 않고, 각 하위 서비스가 자기 트랜잭션을 갖는 것으로 원자성이 충족된다.
 * {@code getPublicFollowingList}/{@code getPublicFollowerList}도 뷰어를 {@code null}로 고정해 같은 조회에
 * 위임하는 것뿐이라 판정이 같다.
 */
@Component
public class FollowService {

    private final FollowCommandUseCase followCommandUseCase;
    private final FollowQueryService followQueryService;

    public FollowService(FollowCommandUseCase followCommandUseCase, FollowQueryService followQueryService) {
        this.followCommandUseCase = followCommandUseCase;
        this.followQueryService = followQueryService;
    }

    public Long follow(FollowCreateCommand command) {
        return followCommandUseCase.follow(command);
    }

    public void unfollow(FollowCancelCommand command) {
        followCommandUseCase.unfollow(command);
    }

    public void removeFollower(FollowerRemoveCommand command) {
        followCommandUseCase.removeFollower(command);
    }

    public boolean isFollowing(Long viewerMemberId, Long targetMemberId) {
        return followQueryService.isFollowing(viewerMemberId, targetMemberId);
    }

    public PaginationResponse<FollowMemberListItemResponse> getFollowingList(Long memberId, Long viewerMemberId, int page, int size) {
        return followQueryService.getFollowingList(memberId, viewerMemberId, page, size);
    }

    public PaginationResponse<FollowMemberListItemResponse> getFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        return followQueryService.getFollowerList(memberId, viewerMemberId, page, size);
    }

    /** 비로그인 공개 목록 — 뷰어가 없으므로 팔로우 여부는 모두 false로 내려간다. */
    public PaginationResponse<FollowMemberListItemResponse> getPublicFollowingList(Long memberId, int page, int size) {
        return followQueryService.getFollowingList(memberId, null, page, size);
    }

    public PaginationResponse<FollowMemberListItemResponse> getPublicFollowerList(Long memberId, int page, int size) {
        return followQueryService.getFollowerList(memberId, null, page, size);
    }

    public PaginationResponse<FollowMemberSearchListItemResponse> searchMembersByNickname(
        String nickname,
        Long viewerMemberId,
        int page,
        int size
    ) {
        return followQueryService.searchMembersByNickname(nickname, viewerMemberId, page, size);
    }
}

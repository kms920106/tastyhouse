package com.tastyhouse.webapi.follow.application.port.in;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.follow.adapter.in.web.response.FollowMemberListItemResponse;
import com.tastyhouse.webapi.follow.adapter.in.web.response.FollowMemberSearchListItemResponse;

/**
 * 팔로우 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code FollowQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface FollowQueryUseCase {

    boolean isFollowing(Long viewerMemberId, Long targetMemberId);

    long countFollowing(Long memberId);

    long countFollower(Long memberId);

    PaginationResponse<FollowMemberListItemResponse> getFollowingList(Long memberId, Long viewerMemberId, int page, int size);

    PaginationResponse<FollowMemberListItemResponse> getFollowerList(Long memberId, Long viewerMemberId, int page, int size);

    PaginationResponse<FollowMemberSearchListItemResponse> searchMembersByNickname(String nickname, Long viewerMemberId, int page, int size);
}

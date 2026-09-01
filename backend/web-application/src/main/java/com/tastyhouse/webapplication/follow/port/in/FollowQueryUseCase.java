package com.tastyhouse.webapplication.follow.port.in;

import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.member.follow.port.out.FollowMemberResult;
import com.tastyhouse.webapplication.follow.port.out.FollowMemberSearchResult;

/**
 * 팔로우 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code FollowQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p>{@code isFollowing}은 응답 래퍼(@code FollowIsFollowingResponse) 없이 <b>평문 boolean</b>을 돌려준다 —
 * 조회 대상 회원 ID는 이미 컨트롤러가 경로 변수로 갖고 있어, 그 두 값을 묶는 일은 표현 계층의 조립이다.
 */
public interface FollowQueryUseCase {

    boolean isFollowing(Long viewerMemberId, Long targetMemberId);

    long countFollowing(Long memberId);

    long countFollower(Long memberId);

    PageResult<FollowMemberResult> getFollowingList(Long memberId, Long viewerMemberId, int page, int size);

    PageResult<FollowMemberResult> getFollowerList(Long memberId, Long viewerMemberId, int page, int size);

    PageResult<FollowMemberSearchResult> searchMembersByNickname(String nickname, Long viewerMemberId, int page, int size);
}

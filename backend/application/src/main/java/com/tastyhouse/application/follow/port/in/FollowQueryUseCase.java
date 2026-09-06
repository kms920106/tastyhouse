package com.tastyhouse.application.follow.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.member.follow.port.out.FollowMemberResult;
import com.tastyhouse.application.follow.port.out.FollowMemberSearchResult;

@WebApp
public interface FollowQueryUseCase {

    boolean isFollowing(Long viewerMemberId, Long targetMemberId);

    long countFollowing(Long memberId);

    long countFollower(Long memberId);

    PageResult<FollowMemberResult> getFollowingList(Long memberId, Long viewerMemberId, int page, int size);

    PageResult<FollowMemberResult> getFollowerList(Long memberId, Long viewerMemberId, int page, int size);

    PageResult<FollowMemberSearchResult> searchMembersByNickname(String nickname, Long viewerMemberId, int page, int size);
}

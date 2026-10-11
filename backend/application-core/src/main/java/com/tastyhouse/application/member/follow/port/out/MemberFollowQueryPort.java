package com.tastyhouse.application.member.follow.port.out;

import java.util.List;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface MemberFollowQueryPort {

    PageResult<FollowMemberResult> findFollowingList(Long memberId, Long viewerMemberId, PageQuery pageQuery);

    PageResult<FollowMemberResult> findFollowerList(Long memberId, Long viewerMemberId, PageQuery pageQuery);

    boolean existsFollow(Long followerId, Long followingId);

    long countFollowing(Long memberId);

    long countFollower(Long memberId);

    List<Long> findFollowingIds(Long followerId);

}

package com.tastyhouse.application.member.follow.port.out;

import java.util.List;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface MemberFollowQueryPort {

    PageResult<FollowMemberResult> findFollowingList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery);

    PageResult<FollowMemberResult> findFollowerList(MemberId memberId, MemberId viewerMemberId, PageQuery pageQuery);

    boolean existsFollow(MemberId followerId, MemberId followingId);

    long countFollowing(MemberId memberId);

    long countFollower(MemberId memberId);

    List<Long> findFollowingIds(MemberId followerId);

}

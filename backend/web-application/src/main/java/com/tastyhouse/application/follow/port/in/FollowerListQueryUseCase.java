package com.tastyhouse.application.follow.port.in;

import com.tastyhouse.application.member.follow.port.out.FollowMemberResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface FollowerListQueryUseCase {

    PageResult<FollowMemberResult> getFollowerList(Long memberId, Long viewerMemberId, int page, int size);
}

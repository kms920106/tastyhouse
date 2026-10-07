package com.tastyhouse.application.follow.port.in;

import com.tastyhouse.application.follow.port.out.FollowMemberSearchResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface FollowMemberSearchQueryUseCase {

    PageResult<FollowMemberSearchResult> searchMembersByNickname(String nickname, Long viewerMemberId, int page, int size);
}

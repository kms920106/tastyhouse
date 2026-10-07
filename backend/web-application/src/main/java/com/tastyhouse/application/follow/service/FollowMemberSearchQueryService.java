package com.tastyhouse.application.follow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.follow.port.in.FollowMemberSearchQueryUseCase;
import com.tastyhouse.application.follow.port.out.FollowMemberSearchResult;
import com.tastyhouse.application.member.follow.port.out.MemberFollowQueryPort;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class FollowMemberSearchQueryService implements FollowMemberSearchQueryUseCase {

    private final MemberFollowQueryPort memberFollowQueryPort;
    private final MemberQueryPort memberQueryPort;

    public FollowMemberSearchQueryService(
        MemberFollowQueryPort memberFollowQueryPort,
        MemberQueryPort memberQueryPort
    ) {
        this.memberFollowQueryPort = memberFollowQueryPort;
        this.memberQueryPort = memberQueryPort;
    }

    @Override
    public PageResult<FollowMemberSearchResult> searchMembersByNickname(
        String nickname,
        Long viewerMemberId,
        int page,
        int size
    ) {
        return memberQueryPort.findByNicknameContaining(nickname, PageQuery.of(page, size))
            .map(result -> new FollowMemberSearchResult(
                result.id(),
                result.nickname(),
                result.memberGrade(),
                result.profileImageUrl(),
                viewerMemberId != null && isFollowing(viewerMemberId, result.id())
            ));
    }

    private boolean isFollowing(Long viewerMemberId, Long targetMemberId) {
        return memberFollowQueryPort.existsFollow(
            MemberId.of(viewerMemberId).value(), MemberId.of(targetMemberId).value()
        );
    }
}

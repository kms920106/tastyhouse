package com.tastyhouse.application.follow.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.member.follow.port.out.FollowMemberResult;
import com.tastyhouse.application.member.follow.port.out.MemberFollowQueryPort;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.application.follow.port.out.FollowMemberSearchResult;
import com.tastyhouse.application.follow.port.in.FollowQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class FollowQueryService implements FollowQueryUseCase {

    private final MemberFollowQueryPort memberFollowQueryPort;
    private final MemberQueryPort memberQueryPort;

    public FollowQueryService(
        MemberFollowQueryPort memberFollowQueryPort,
        MemberQueryPort memberQueryPort
    ) {
        this.memberFollowQueryPort = memberFollowQueryPort;
        this.memberQueryPort = memberQueryPort;
    }

    @Override
    public boolean isFollowing(Long viewerMemberId, Long targetMemberId) {
        return memberFollowQueryPort.existsFollow(
            MemberId.of(viewerMemberId), MemberId.of(targetMemberId)
        );
    }

    @Override
    public long countFollowing(Long memberId) {
        return memberFollowQueryPort.countFollowing(MemberId.of(memberId));
    }

    @Override
    public long countFollower(Long memberId) {
        return memberFollowQueryPort.countFollower(MemberId.of(memberId));
    }

    @Override
    public PageResult<FollowMemberResult> getFollowingList(Long memberId, Long viewerMemberId, int page, int size) {
        return memberFollowQueryPort.findFollowingList(
            MemberId.of(memberId), toViewerId(viewerMemberId), PageQuery.of(page, size)
        );
    }

    @Override
    public PageResult<FollowMemberResult> getFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        return memberFollowQueryPort.findFollowerList(
            MemberId.of(memberId), toViewerId(viewerMemberId), PageQuery.of(page, size)
        );
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
                result.memberGrade().name(),
                result.profileImageUrl(),
                viewerMemberId != null && isFollowing(viewerMemberId, result.id())
            ));
    }

    private MemberId toViewerId(Long viewerMemberId) {
        return viewerMemberId == null ? null : MemberId.of(viewerMemberId);
    }
}

package com.tastyhouse.core.domain.member.follow.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.follow.domain.repository.MemberFollowRepository;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.domain.member.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberFollowQueryService {

    private final MemberFollowRepository memberFollowRepository;
    private final MemberQueryService memberQueryService;

    public PageResult<FollowMemberResult> findFollowingList(MemberId memberId, MemberId viewerMemberId, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return memberFollowRepository.findFollowingList(memberId, viewerMemberId, pageQuery);
    }

    public PageResult<FollowMemberResult> findFollowerList(MemberId memberId, MemberId viewerMemberId, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return memberFollowRepository.findFollowerList(memberId, viewerMemberId, pageQuery);
    }

    public PageResult<MemberWithProfileImageResult> findMembersByNicknameContaining(String nickname, int page, int size) {
        return memberQueryService.findByNicknameContaining(nickname, page, size);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return memberFollowRepository.existsByFollowerIdAndFollowingId(MemberId.of(followerId), MemberId.of(followingId));
    }

    public long countFollowing(MemberId memberId) {
        return memberFollowRepository.countByFollowerId(memberId);
    }

    public long countFollower(MemberId memberId) {
        return memberFollowRepository.countByFollowingId(memberId);
    }

    public List<Long> findFollowingIds(MemberId memberId) {
        return memberFollowRepository.findFollowingIdsByFollowerId(memberId);
    }
}

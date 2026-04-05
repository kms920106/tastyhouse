package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.service.FollowCoreService;
import com.tastyhouse.core.service.MemberCoreService;
import com.tastyhouse.core.service.ReviewCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
import com.tastyhouse.webapi.member.response.OtherMemberProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberFollowService {

    private final MemberCoreService memberCoreService;
    private final ReviewCoreService reviewCoreService;
    private final FollowCoreService followCoreService;
    private final FileService fileService;

    // 다른 회원의 프로필과 현재 사용자의 팔로우 여부를 조회
    @Transactional(readOnly = true)
    public OtherMemberProfileResponse getOtherMemberProfile(Long targetMemberId, Long viewerMemberId) {
        Member member = memberCoreService.getById(targetMemberId);

        String profileImageUrl = null;
        if (member.getProfileImageFileId() != null) {
            profileImageUrl = fileService.getFileUrl(member.getProfileImageFileId());
        }

        boolean isFollowing = viewerMemberId != null && followCoreService.isFollowing(viewerMemberId, targetMemberId);

        return new OtherMemberProfileResponse(member.getId(), member.getNickname(), member.getMemberGrade(), member.getStatusMessage(), profileImageUrl, isFollowing);
    }

    // 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회
    @Transactional(readOnly = true)
    public MemberStatsResponse getMemberStats(Long memberId) {
        long reviewCount = reviewCoreService.countByMemberIdAndIsHiddenFalse(memberId);
        long followingCount = followCoreService.countFollowing(memberId);
        long followerCount = followCoreService.countFollower(memberId);

        return new MemberStatsResponse(reviewCount, followingCount, followerCount);
    }
}

package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.entity.user.dto.MemberWithProfileImageDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.FollowCoreService;
import com.tastyhouse.core.service.MemberCoreService;
import com.tastyhouse.core.service.ReviewCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.response.MemberStatsResponse;
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

    // 회원의 프로필 조회
    @Transactional(readOnly = true)
    public MemberProfileResponse getMemberProfile(Long targetMemberId) {
        MemberWithProfileImageDto dto = memberCoreService.findMemberWithProfileImageById(targetMemberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberProfileResponse.from(
            dto.id(),
            dto.nickname(),
            dto.memberGrade(),
            dto.statusMessage(),
            fileService.getUrlByPath(dto.profileImageFilePath())
        );
    }

    // 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회
    @Transactional(readOnly = true)
    public MemberStatsResponse getMemberStats(Long memberId) {
        long reviewCount = reviewCoreService.countVisibleReviewsByMemberId(memberId);
        long followingCount = followCoreService.countFollowing(memberId);
        long followerCount = followCoreService.countFollower(memberId);

        return MemberStatsResponse.from(
            reviewCount,
            followingCount,
            followerCount
        );
    }
}

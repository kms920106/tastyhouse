package com.tastyhouse.webapi.follow;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.service.FollowCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.follow.response.FollowMemberResponse;
import com.tastyhouse.webapi.follow.response.MemberSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowCoreService followCoreService;
    private final FileService fileService;

    @Transactional
    public void follow(Long followerId, Long followingId) {
        followCoreService.follow(followerId, followingId);
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        followCoreService.unfollow(followerId, followingId);
    }

    @Transactional
    public void removeFollower(Long memberId, Long followerId) {
        followCoreService.removeFollower(memberId, followerId);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long viewerMemberId, Long targetMemberId) {
        return followCoreService.isFollowing(viewerMemberId, targetMemberId);
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberResponse> getFollowingList(Long memberId, Long viewerMemberId, int page, int size) {
        return PageResult.from(followCoreService.findFollowingList(memberId, viewerMemberId, page, size))
            .map(dto -> FollowMemberResponse.of(dto, fileService.getUrlByPath(dto.profileImageFilePath())));
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberResponse> getFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        return PageResult.from(followCoreService.findFollowerList(memberId, viewerMemberId, page, size))
            .map(dto -> FollowMemberResponse.of(dto, fileService.getUrlByPath(dto.profileImageFilePath())));
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberResponse> getPublicFollowingList(Long memberId, int page, int size) {
        return PageResult.from(followCoreService.findFollowingList(memberId, null, page, size))
            .map(dto -> FollowMemberResponse.of(dto, fileService.getUrlByPath(dto.profileImageFilePath())));
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberResponse> getPublicFollowerList(Long memberId, int page, int size) {
        return PageResult.from(followCoreService.findFollowerList(memberId, null, page, size))
            .map(dto -> FollowMemberResponse.of(dto, fileService.getUrlByPath(dto.profileImageFilePath())));
    }

    @Transactional(readOnly = true)
    public PageResult<MemberSearchResponse> searchMembersByNickname(String nickname, Long viewerMemberId, int page, int size) {
        return PageResult.from(followCoreService.findMembersByNicknameContaining(nickname, page, size))
            .map(dto -> {
                String profileImageUrl = fileService.getUrlByPath(dto.profileImageFilePath());
                boolean isFollowing = viewerMemberId != null && followCoreService.isFollowing(viewerMemberId, dto.id());
                return MemberSearchResponse.of(
                    dto.id(),
                    dto.nickname(),
                    dto.memberGrade(),
                    profileImageUrl,
                    isFollowing
                );
            });
    }
}

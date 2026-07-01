package com.tastyhouse.webapi.follow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.follow.application.FollowCommandService;
import com.tastyhouse.core.domain.follow.application.FollowQueryService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.webapi.follow.response.FollowMemberListItemResponse;
import com.tastyhouse.webapi.follow.response.MemberSearchListItemResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowCommandService followCommandService;
    private final FollowQueryService followQueryService;
    private final FileService fileService;

    @Transactional
    public void follow(Long followerId, Long followingId) {
        followCommandService.follow(followerId, followingId);
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        followCommandService.unfollow(followerId, followingId);
    }

    @Transactional
    public void removeFollower(Long memberId, Long followerId) {
        followCommandService.removeFollower(memberId, followerId);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long viewerMemberId, Long targetMemberId) {
        return followQueryService.isFollowing(viewerMemberId, targetMemberId);
    }

    @Transactional(readOnly = true)
    public PageResponse<FollowMemberListItemResponse> getFollowingList(Long memberId, Long viewerMemberId, int page, int size) {
        return PageResponse.from(followQueryService.findFollowingList(memberId, viewerMemberId, page, size))
            .map(dto -> FollowMemberListItemResponse.of(dto, fileService.getUrlByPath(dto.profileImageFilePath())));
    }

    @Transactional(readOnly = true)
    public PageResponse<FollowMemberListItemResponse> getFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        return PageResponse.from(followQueryService.findFollowerList(memberId, viewerMemberId, page, size))
            .map(dto -> FollowMemberListItemResponse.of(dto, fileService.getUrlByPath(dto.profileImageFilePath())));
    }

    @Transactional(readOnly = true)
    public PageResponse<FollowMemberListItemResponse> getPublicFollowingList(Long memberId, int page, int size) {
        return PageResponse.from(followQueryService.findFollowingList(memberId, null, page, size))
            .map(dto -> FollowMemberListItemResponse.of(dto, fileService.getUrlByPath(dto.profileImageFilePath())));
    }

    @Transactional(readOnly = true)
    public PageResponse<FollowMemberListItemResponse> getPublicFollowerList(Long memberId, int page, int size) {
        return PageResponse.from(followQueryService.findFollowerList(memberId, null, page, size))
            .map(dto -> FollowMemberListItemResponse.of(dto, fileService.getUrlByPath(dto.profileImageFilePath())));
    }

    @Transactional(readOnly = true)
    public PageResponse<MemberSearchListItemResponse> searchMembersByNickname(String nickname, Long viewerMemberId, int page, int size) {
        return PageResponse.from(followQueryService.findMembersByNicknameContaining(nickname, page, size))
            .map(dto -> {
                String profileImageUrl = fileService.getUrlByPath(dto.profileImageFilePath());
                boolean isFollowing = viewerMemberId != null && followQueryService.isFollowing(viewerMemberId, dto.id());
                return MemberSearchListItemResponse.of(
                    dto.id(),
                    dto.nickname(),
                    dto.memberGrade(),
                    profileImageUrl,
                    isFollowing
                );
            });
    }
}

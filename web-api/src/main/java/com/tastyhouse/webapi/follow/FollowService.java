package com.tastyhouse.webapi.follow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.follow.application.MemberFollowCommandService;
import com.tastyhouse.core.domain.member.follow.application.MemberFollowQueryService;
import com.tastyhouse.core.domain.member.follow.application.dto.result.FollowMemberResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.follow.response.FollowMemberListItemResponse;
import com.tastyhouse.webapi.follow.response.FollowMemberSearchListItemResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final MemberFollowCommandService followCommandService;
    private final MemberFollowQueryService followQueryService;
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
        MemberId memberIdVo = MemberId.of(memberId);
        MemberId followerIdVo = MemberId.of(followerId);
        followCommandService.removeFollower(memberIdVo, followerIdVo);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long viewerMemberId, Long targetMemberId) {
        return followQueryService.isFollowing(viewerMemberId, targetMemberId);
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberListItemResponse> getFollowingList(Long memberId, Long viewerMemberId, int page, int size) {
        return followQueryService.findFollowingList(MemberId.of(memberId), MemberId.of(viewerMemberId), page, size)
            .map(this::toFollowMemberListItemResponse);
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberListItemResponse> getFollowerList(Long memberId, Long viewerMemberId, int page, int size) {
        return followQueryService.findFollowerList(MemberId.of(memberId), MemberId.of(viewerMemberId), page, size)
            .map(this::toFollowMemberListItemResponse);
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberListItemResponse> getPublicFollowingList(Long memberId, int page, int size) {
        return followQueryService.findFollowingList(MemberId.of(memberId), null, page, size)
            .map(this::toFollowMemberListItemResponse);
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberListItemResponse> getPublicFollowerList(Long memberId, int page, int size) {
        return followQueryService.findFollowerList(MemberId.of(memberId), null, page, size)
            .map(this::toFollowMemberListItemResponse);
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberSearchListItemResponse> searchMembersByNickname(String nickname, Long viewerMemberId, int page, int size) {
        return followQueryService.findMembersByNicknameContaining(nickname, page, size)
            .map(dto -> {
                String profileImageUrl = fileService.getUrlByPath(dto.profileImageFilePath());
                boolean isFollowing = viewerMemberId != null && followQueryService.isFollowing(viewerMemberId, dto.id());
                return FollowMemberSearchListItemResponse.of(
                    dto.id(),
                    dto.nickname(),
                    dto.memberGrade().name(),
                    profileImageUrl,
                    isFollowing
                );
            });
    }

    private FollowMemberListItemResponse toFollowMemberListItemResponse(FollowMemberResult dto) {
        String profileImageUrl = fileService.getUrlByPath(dto.profileImageFilePath());
        return FollowMemberListItemResponse.of(
            dto.memberId(),
            dto.nickname(),
            dto.memberGrade().name(),
            profileImageUrl,
            dto.following()
        );
    }
}

package com.tastyhouse.webapi.follow;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.follow.dto.FollowMemberDto;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.service.FollowCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.common.PageRequest;
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
    public PageResult<FollowMemberResponse> getFollowingList(Long memberId, Long viewerMemberId, PageRequest pageRequest) {
        PageResult<FollowMemberDto> coreResult = followCoreService.findFollowingList(memberId, viewerMemberId, pageRequest.page(), pageRequest.size());
        return coreResult.map(dto -> FollowMemberResponse.of(dto, resolveProfileImageUrl(dto.profileImageFileId())));
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberResponse> getFollowerList(Long memberId, Long viewerMemberId, PageRequest pageRequest) {
        PageResult<FollowMemberDto> coreResult = followCoreService.findFollowerList(memberId, viewerMemberId, pageRequest.page(), pageRequest.size());
        return coreResult.map(dto -> FollowMemberResponse.of(dto, resolveProfileImageUrl(dto.profileImageFileId())));
    }

    @Transactional(readOnly = true)
    public PageResult<MemberSearchResponse> searchMembersByNickname(String nickname, Long viewerMemberId, PageRequest pageRequest) {
        PageResult<Member> coreResult = followCoreService.findMembersByNicknameContaining(nickname, pageRequest.page(), pageRequest.size());
        return coreResult.map(member -> {
            String profileImageUrl = resolveProfileImageUrl(member.getProfileImageFileId());
            boolean isFollowing = viewerMemberId != null && followCoreService.isFollowing(viewerMemberId, member.getId());
            return MemberSearchResponse.of(member, profileImageUrl, isFollowing);
        });
    }

    private String resolveProfileImageUrl(Long profileImageFileId) {
        if (profileImageFileId == null) {
            return null;
        }
        return fileService.getFileUrl(profileImageFileId);
    }
}

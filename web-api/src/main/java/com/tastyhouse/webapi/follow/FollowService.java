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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        org.springframework.data.domain.PageRequest springPageRequest =
            org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());

        Page<FollowMemberDto> page = followCoreService.findFollowingList(memberId, viewerMemberId, springPageRequest);

        List<FollowMemberResponse> content = page.getContent().stream()
            .map(dto -> {
                String profileImageUrl = resolveProfileImageUrl(dto.profileImageFileId());
                return FollowMemberResponse.of(dto, profileImageUrl);
            })
            .collect(Collectors.toList());

        return new PageResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberResponse> getFollowerList(Long memberId, Long viewerMemberId, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPageRequest =
            org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());

        Page<FollowMemberDto> page = followCoreService.findFollowerList(memberId, viewerMemberId, springPageRequest);

        List<FollowMemberResponse> content = page.getContent().stream()
            .map(dto -> {
                String profileImageUrl = resolveProfileImageUrl(dto.profileImageFileId());
                return FollowMemberResponse.of(dto, profileImageUrl);
            })
            .collect(Collectors.toList());

        return new PageResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    @Transactional(readOnly = true)
    public PageResult<MemberSearchResponse> searchMembersByNickname(String nickname, Long viewerMemberId, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPageRequest =
            org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());

        Page<Member> page = followCoreService.findMembersByNicknameContaining(nickname, springPageRequest);

        List<MemberSearchResponse> content = page.getContent().stream()
            .map(member -> {
                String profileImageUrl = resolveProfileImageUrl(member.getProfileImageFileId());
                boolean isFollowing = viewerMemberId != null
                    && followCoreService.isFollowing(viewerMemberId, member.getId());
                return MemberSearchResponse.of(member, profileImageUrl, isFollowing);
            })
            .collect(Collectors.toList());

        return new PageResult<>(content, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    private String resolveProfileImageUrl(Long profileImageFileId) {
        if (profileImageFileId == null) {
            return null;
        }
        return fileService.getFileUrl(profileImageFileId);
    }
}

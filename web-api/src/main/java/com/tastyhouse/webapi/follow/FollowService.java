package com.tastyhouse.webapi.follow;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.follow.Follow;
import com.tastyhouse.core.entity.follow.dto.FollowMemberDto;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.follow.FollowRepository;
import com.tastyhouse.core.repository.member.MemberRepository;
import com.tastyhouse.file.FileService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;

    @Transactional
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        if (!memberRepository.existsById(followingId)) {
            throw new EntityNotFoundException(ErrorCode.FOLLOW_TARGET_NOT_FOUND);
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        followRepository.save(Follow.of(followerId, followingId));
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }

    @Transactional
    public void removeFollower(Long memberId, Long followerId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public PageResult<FollowMemberResponse> getFollowingList(Long memberId, Long viewerMemberId, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPageRequest =
            org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());

        Page<FollowMemberDto> page = followRepository.findFollowingList(memberId, viewerMemberId, springPageRequest);

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

        Page<FollowMemberDto> page = followRepository.findFollowerList(memberId, viewerMemberId, springPageRequest);

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

        Page<Member> page = memberRepository.findByNicknameContaining(nickname, springPageRequest);

        List<MemberSearchResponse> content = page.getContent().stream()
            .map(member -> {
                String profileImageUrl = resolveProfileImageUrl(member.getProfileImageFileId());
                boolean isFollowing = viewerMemberId != null
                    && followRepository.existsByFollowerIdAndFollowingId(viewerMemberId, member.getId());
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

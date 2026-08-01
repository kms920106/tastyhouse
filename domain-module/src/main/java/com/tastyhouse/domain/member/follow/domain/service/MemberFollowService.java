package com.tastyhouse.domain.member.follow.domain.service;

import com.tastyhouse.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.member.follow.domain.model.MemberFollow;
import com.tastyhouse.domain.member.follow.domain.repository.MemberFollowRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 팔로우 관계 등록·해제(도메인 서비스).
 *
 * <p>팔로우 등록은 {@code MemberFollow} 애그리거트를 만들기 전에 다른 애그리거트 타입인
 * {@code Member}(팔로우 대상)의 존재를 확인해야 하므로, 두 애그리거트 타입을 함께 다루는 불변식
 * 오케스트레이션(분류 C)이다. "자기 자신 팔로우 금지", "대상 회원이 존재해야 함", "중복 팔로우 금지"
 * 규칙이 호출 경로마다 갈리지 않도록 도메인 계층에 둔다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다.
 */
public class MemberFollowService {

    private final MemberFollowRepository memberFollowRepository;
    private final MemberRepository memberRepository;

    public MemberFollowService(
        MemberFollowRepository memberFollowRepository,
        MemberRepository memberRepository
    ) {
        this.memberFollowRepository = memberFollowRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * @return 생성된 팔로우 관계의 식별자
     */
    public Long follow(MemberId followerId, MemberId followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        if (memberRepository.findById(followingId).isEmpty()) {
            throw new EntityNotFoundException(ErrorCode.FOLLOW_TARGET_NOT_FOUND);
        }

        if (memberFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        MemberFollow saved = memberFollowRepository.save(MemberFollow.of(followerId, followingId));
        return saved.getId();
    }

    public void unfollow(MemberId followerId, MemberId followingId) {
        MemberFollow memberFollow = memberFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        memberFollowRepository.delete(memberFollow);
    }

    /**
     * 내 팔로워를 끊는다 — 대상이 나(memberId)를 팔로우하던 관계를 삭제한다.
     */
    public void removeFollower(MemberId memberId, MemberId followerId) {
        MemberFollow memberFollow = memberFollowRepository.findByFollowerIdAndFollowingId(followerId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        memberFollowRepository.delete(memberFollow);
    }
}

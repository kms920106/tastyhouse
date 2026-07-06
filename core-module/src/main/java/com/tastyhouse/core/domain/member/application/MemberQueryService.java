package com.tastyhouse.core.domain.member.application;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberSocialAccount;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.domain.model.SocialProvider;
import com.tastyhouse.core.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.core.domain.member.domain.repository.MemberSocialAccountRepository;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryService {

    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;

    public Optional<Member> findById(MemberId memberId) {
        return memberRepository.findById(memberId);
    }

    public Member getById(MemberId memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Member getById(Long memberId) {
        return getById(MemberId.of(memberId));
    }

    public boolean existsByUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public boolean existsByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
        return memberRepository.existsByPhoneNumberAndStatusNot(phoneNumber, memberStatus);
    }

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
        return memberRepository.findByPhoneNumberAndStatusNot(phoneNumber, memberStatus);
    }

    public PageResult<MemberWithProfileImageResult> findByNicknameContaining(String nickname, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return memberRepository.findByNicknameContaining(nickname, pageQuery);
    }

    public Optional<MemberWithProfileImageResult> findMemberWithProfileImage(MemberId memberId) {
        return memberRepository.findMemberWithProfileImageById(memberId);
    }

    public Map<Long, MemberWithProfileImageResult> findMemberWithProfileImagesByIds(Collection<Long> memberIds) {
        return memberIds.stream()
            .map(id -> memberRepository.findMemberWithProfileImageById(MemberId.of(id)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toMap(MemberWithProfileImageResult::id, result -> result));
    }

    public Optional<MemberSocialAccount> findSocialAccount(SocialProvider provider, String providerId) {
        return memberSocialAccountRepository.findByProviderAndProviderId(provider, providerId);
    }

    public boolean existsSocialAccount(SocialProvider provider, String providerId) {
        return memberSocialAccountRepository.existsByProviderAndProviderId(provider, providerId);
    }
}

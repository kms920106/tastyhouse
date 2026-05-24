package com.tastyhouse.core.service;

import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.MemberGrade;
import com.tastyhouse.core.entity.user.MemberWithdrawal;
import com.tastyhouse.core.entity.user.dto.MemberWithProfileImageDto;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.member.MemberJpaRepository;
import com.tastyhouse.core.repository.member.MemberRepository;
import com.tastyhouse.core.repository.member.MemberWithdrawalJpaRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCoreService {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;
    private final MemberWithdrawalJpaRepository memberWithdrawalJpaRepository;

    @Transactional(readOnly = true)
    public Optional<Member> findById(Long memberId) {
        return memberJpaRepository.findById(memberId);
    }

    @Transactional(readOnly = true)
    public Member getById(Long memberId) {
        return memberJpaRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    @Transactional(readOnly = true)
    public boolean existsByPhoneNumberValueAndMemberStatusNot(String phoneNumber, com.tastyhouse.core.entity.user.MemberStatus memberStatus) {
        return memberRepository.existsByPhoneNumberValueAndMemberStatusNot(phoneNumber, memberStatus);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByPhoneNumberAndStatusNot(String phoneNumber, com.tastyhouse.core.entity.user.MemberStatus memberStatus) {
        return memberRepository.findByPhoneNumberValueAndMemberStatusNot(phoneNumber, memberStatus);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByNickname(String nickname) {
        return memberRepository.findByNickname(nickname);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    @Transactional
    public long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade) {
        return memberRepository.bulkUpdateGrade(memberIds, grade);
    }

    @Transactional
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    @Transactional
    public void saveWithdrawal(@NonNull MemberWithdrawal memberWithdrawal) {
        memberWithdrawalJpaRepository.save(memberWithdrawal);
    }

    @Transactional(readOnly = true)
    public Optional<MemberWithProfileImageDto> findMemberWithProfileImageById(Long memberId) {
        return memberRepository.findMemberWithProfileImageById(memberId);
    }

}

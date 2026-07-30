package com.tastyhouse.core.domain.member.domain.service;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.event.MemberRegisteredEvent;
import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberGender;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.core.domain.member.referral.domain.service.ReferralRegistrationService;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.event.DomainEventPublisher;

/**
 * 회원 등록(도메인 서비스).
 *
 * <p>가입은 한 트랜잭션에서 {@code Member} 애그리거트를 저장한 뒤, 추천인 닉네임이 있으면 그 회원을
 * 조회해 {@code MemberReferral} 애그리거트까지 함께 만든다. 애그리거트 타입 2개 이상을 함께
 * load &amp; save 하는 불변식 오케스트레이션(분류 C)이므로 소비 모듈의 command 서비스가 아니라 도메인
 * 계층에 둔다 — 일반 가입(web)과 소셜 가입(web 소셜로그인 4종)이 서로 다른 경로에서 호출하더라도
 * "아이디·닉네임·휴대폰 중복 금지", "자기 자신을 추천인으로 지정 금지" 같은 규칙이 갈리지 않게 한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 호출자(command 서비스)의
 * 트랜잭션 안에서 실행된다.
 *
 * <p>비밀번호는 이미 인코딩된 값을 받는다 — 인코딩은 Spring Security {@code PasswordEncoder}에
 * 의존하므로 프레임워크-프리 도메인 계층이 아니라 소비 모듈(web-api)이 담당한다.
 */
public class MemberRegistrationService {

    private final MemberRepository memberRepository;
    private final ReferralRegistrationService referralRegistrationService;
    private final DomainEventPublisher domainEventPublisher;

    public MemberRegistrationService(
        MemberRepository memberRepository,
        ReferralRegistrationService referralRegistrationService,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberRepository = memberRepository;
        this.referralRegistrationService = referralRegistrationService;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 아이디·비밀번호 기반 일반 가입. 아이디/닉네임/휴대폰 중복을 검증한 뒤 저장하고, 추천인이 지정되면
     * 추천 관계까지 등록한다.
     */
    public void signUp(
        String username,
        String encodedPassword,
        String nickname,
        String fullName,
        MemberGender gender,
        Integer birthDate,
        String phoneNumber,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        String referrerNickname
    ) {
        if (memberRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.MEMBER_USERNAME_DUPLICATED);
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }
        if (memberRepository.existsByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_ALREADY_REGISTERED);
        }

        Member member = memberRepository.save(Member.of(
            username, encodedPassword, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled
        ));

        registerReferralIfPresent(member, nickname, referrerNickname);
        publishRegistered(member);
    }

    /**
     * 소셜 가입. 소셜 제공자가 신원을 보증하므로 비밀번호가 없고 중복 검증도 호출자(소셜로그인 서비스)가
     * 소셜 계정 존재 여부로 이미 수행하므로 여기서는 반복하지 않는다.
     */
    public Member signUpSocial(
        String username,
        String nickname,
        String fullName,
        MemberGender gender,
        Integer birthDate,
        String phoneNumber,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        String referrerNickname
    ) {
        Member member = memberRepository.save(Member.ofSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled
        ));

        registerReferralIfPresent(member, nickname, referrerNickname);
        publishRegistered(member);

        return member;
    }

    private void registerReferralIfPresent(Member member, String nickname, String referrerNickname) {
        if (referrerNickname == null || referrerNickname.isBlank()) {
            return;
        }
        if (referrerNickname.equals(nickname)) {
            throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
        }

        Member referrer = memberRepository.findByNickname(referrerNickname)
            .orElseThrow(() -> new BusinessException(ErrorCode.REFERRAL_REFERRER_NOT_FOUND));

        referralRegistrationService.register(referrer.getMemberId(), member.getMemberId());
    }

    private void publishRegistered(Member member) {
        domainEventPublisher.publish(new MemberRegisteredEvent(
            member.getMemberId(), member.getUsername(), LocalDateTime.now()
        ));
    }
}

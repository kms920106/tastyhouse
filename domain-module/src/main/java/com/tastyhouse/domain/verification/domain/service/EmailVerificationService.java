package com.tastyhouse.domain.verification.domain.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.domain.verification.domain.event.EmailVerifiedEvent;
import com.tastyhouse.domain.verification.domain.model.EmailVerification;
import com.tastyhouse.domain.verification.domain.model.EmailVerificationStatus;
import com.tastyhouse.domain.verification.domain.repository.EmailVerificationRepository;
import com.tastyhouse.domain.verification.domain.vo.VerificationCode;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 이메일 인증 발급·검증 규칙(도메인 서비스).
 *
 * <p>인증코드 발급은 "같은 이메일의 기존 미완료 인증을 모두 만료시킨 뒤 새 인증을 저장한다"는,
 * 같은 애그리거트 타입의 여러 인스턴스를 한 트랜잭션에서 함께 다루는 불변식이다(공통 지침 분류 C).
 * 검증(코드 대조 + 상태 전이 + 저장 + 이벤트 발행) 역시 회원가입 인증과 비밀번호 재설정 인증이
 * 공유해야 하는 규칙이므로 특정 액터의 command 서비스가 아니라 도메인 계층에 둔다 — 소비 모듈마다
 * 복제하면 만료 판정·상태 전이 규칙이 갈릴 수 있다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 호출자(소비 모듈의 command
 * 서비스·파사드)의 트랜잭션 안에서 실행된다. 이벤트 발행은 Spring {@code ApplicationEventPublisher}가
 * 아니라 프레임워크-프리 포트인 {@link DomainEventPublisher}를 통해 수행한다.
 *
 * <p>가입 중복 검증에 {@link MemberRepository}를 직접 주입하는 것은 "이 조회가 없으면 불변식 검증이
 * 불가능한가?"라는 write 포트 잔류 판정에 부합한다(공통 지침) — 화면 조립용 read가 아니라 발급
 * 가능 여부를 가르는 검증이다.
 */
public class EmailVerificationService {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final DomainEventPublisher domainEventPublisher;

    public EmailVerificationService(
        MemberRepository memberRepository,
        EmailVerificationRepository emailVerificationRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberRepository = memberRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 회원가입용 인증코드를 발급한다. 이미 가입된 이메일이면 발급을 거부한다.
     */
    public EmailVerification issueForSignUp(String email) {
        if (memberRepository.existsByUsername(email)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_REGISTERED);
        }
        return issue(email);
    }

    /**
     * 인증코드를 발급한다. 같은 이메일의 기존 미완료 인증은 모두 만료시킨다.
     *
     * <p>발급된 코드 값을 발송 문구에 담아야 하는 호출부(비밀번호 재설정 등)를 위해 저장된
     * 인증 애그리거트를 반환한다.
     */
    public EmailVerification issue(String email) {
        emailVerificationRepository.expireAllPendingByEmail(email);
        return emailVerificationRepository.save(EmailVerification.create(email));
    }

    /**
     * 회원가입용 인증코드를 검증하고, 완료 후 {@link EmailVerifiedEvent}를 발행한다.
     *
     * <p>이벤트를 이 경로에만 두는 이유: 비밀번호 재설정도 같은 코드 대조·상태 전이 규칙을
     * 공유하지만(→ {@link #confirm}) "이메일 인증 완료"라는 사실이 성립하는 것은 회원가입
     * 흐름뿐이다. 검증 로직에 발행을 묶으면 재설정 트래픽까지 이 이벤트를 발생시켜, 향후
     * 구독자(가입 쿠폰 지급·퍼널 집계 등)가 재설정을 가입으로 오인하게 된다.
     */
    public void confirmForSignUp(String email, String verificationCode) {
        EmailVerification verification = confirm(email, verificationCode);

        domainEventPublisher.publish(new EmailVerifiedEvent(
            verification.getEmailVerificationId(),
            email,
            verification.getVerifiedAt()
        ));
    }

    /**
     * 발급된 인증코드를 검증하고 인증 완료 상태로 전이시킨다. 이벤트는 발행하지 않는다.
     *
     * <p>도메인이 프레임워크-프리라 더티 체킹이 없으므로 상태 전이 후 명시적으로 저장한다.
     * 발행 여부를 흐름별로 가르기 위해(→ {@link #confirmForSignUp}) 전이된 애그리거트를 반환한다.
     */
    public EmailVerification confirm(String email, String verificationCode) {
        EmailVerification verification = emailVerificationRepository
            .findLatestPendingByEmail(email, EmailVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND));

        verification.verify(VerificationCode.of(verificationCode), LocalDateTime.now());
        return emailVerificationRepository.save(verification);
    }
}

package com.tastyhouse.domain.mail.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.mail.event.MailVerifiedEvent;
import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationPurpose;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;
import com.tastyhouse.domain.mail.port.MailSender;
import com.tastyhouse.domain.mail.repository.MailVerificationRepository;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;
import com.tastyhouse.domain.shared.vo.VerificationCode;

/**
 * 메일 인증 발급·검증 규칙(도메인 서비스).
 *
 * <p>인증코드 발급은 "같은 이메일의 기존 미완료 인증을 모두 만료시킨 뒤 새 인증을 저장하고
 * 발송한다"는, 같은 애그리거트 타입의 여러 인스턴스를 한 트랜잭션에서 함께 다루는
 * 불변식이다(공통 지침 분류 C). 검증(코드 대조 + 상태 전이 + 저장 + 이벤트 발행) 역시 회원가입
 * 인증과 비밀번호 재설정 인증이 공유해야 하는 규칙이므로 특정 액터의 command 서비스가 아니라
 * 도메인 계층에 둔다 — 소비 모듈마다 복제하면 만료 판정·상태 전이 규칙이 갈릴 수 있다.
 *
 * <p><b>발송이 발급에 포함되는 이유</b>: 인증코드는 발송되지 않으면 존재 가치가 없다. 과거에는
 * 발송 책임이 호출부에 흩어져 있어(비밀번호 재설정 파사드만 발송을 호출) 회원가입 인증코드 발송
 * API가 코드를 저장만 하고 발송하지 않는 버그가 있었다. {@link #issue}가 저장과 발송을 함께
 * 수행하면 그 누락이 구조적으로 불가능해지고, 발송 실패 시 인증 레코드가 롤백되어 "코드는
 * 있는데 메일은 오지 않은" 유령 레코드가 남지 않는다.
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
public class MailVerificationService {

    private final MemberRepository memberRepository;
    private final MailVerificationRepository mailVerificationRepository;
    private final MailSender mailSender;
    private final DomainEventPublisher domainEventPublisher;

    public MailVerificationService(
        MemberRepository memberRepository,
        MailVerificationRepository mailVerificationRepository,
        MailSender mailSender,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberRepository = memberRepository;
        this.mailVerificationRepository = mailVerificationRepository;
        this.mailSender = mailSender;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 회원가입용 인증코드를 발급·발송한다. 이미 가입된 이메일이면 발급을 거부한다.
     *
     * <p>발급된 애그리거트를 반환하지 않는 이유: 회원가입 흐름은 코드 값을 호출부로 되돌릴 필요가
     * 없다(코드는 메일로만 전달된다). 코드 값이 필요한 경로는 {@link #issue}를 직접 호출한다.
     */
    public void issueForSignUp(String email) {
        if (memberRepository.existsByUsername(email)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_REGISTERED);
        }
        issue(email, MailVerificationPurpose.SIGN_UP);
    }

    /**
     * 인증코드를 발급하고 메일로 발송한다. 같은 이메일의 기존 미완료 인증은 모두 만료시킨다.
     *
     * <p>발송이 실패하면 예외가 전파되어 인증 레코드 저장도 함께 롤백된다(위 클래스 Javadoc 참고).
     * 발급된 코드 값이 필요한 호출부를 위해 저장된 인증 애그리거트를 반환한다.
     */
    public MailVerification issue(String email, MailVerificationPurpose purpose) {
        mailVerificationRepository.expireAllPendingByEmail(email);
        MailVerification saved = mailVerificationRepository.save(MailVerification.create(email));

        mailSender.send(
            email,
            MailVerificationMessage.subject(purpose),
            MailVerificationMessage.body(purpose, saved.getVerificationCode())
        );

        return saved;
    }

    /**
     * 회원가입용 인증코드를 검증하고, 완료 후 {@link MailVerifiedEvent}를 발행한다.
     *
     * <p>이벤트를 이 경로에만 두는 이유: 비밀번호 재설정도 같은 코드 대조·상태 전이 규칙을
     * 공유하지만(→ {@link #confirm}) "메일 인증 완료"라는 사실이 성립하는 것은 회원가입
     * 흐름뿐이다. 검증 로직에 발행을 묶으면 재설정 트래픽까지 이 이벤트를 발생시켜, 향후
     * 구독자(가입 쿠폰 지급·퍼널 집계 등)가 재설정을 가입으로 오인하게 된다.
     */
    public void confirmForSignUp(String email, String verificationCode) {
        MailVerification verification = confirm(email, verificationCode);

        domainEventPublisher.publish(new MailVerifiedEvent(
            verification.getMailVerificationId(),
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
    public MailVerification confirm(String email, String verificationCode) {
        MailVerification verification = mailVerificationRepository
            .findLatestPendingByEmail(email, MailVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.MAIL_VERIFICATION_CODE_NOT_FOUND));

        verification.verify(VerificationCode.of(verificationCode), LocalDateTime.now());
        return mailVerificationRepository.save(verification);
    }
}

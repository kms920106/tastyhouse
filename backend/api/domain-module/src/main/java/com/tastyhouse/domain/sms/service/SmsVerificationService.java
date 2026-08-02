package com.tastyhouse.domain.sms.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.sms.event.SmsVerifiedEvent;
import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;
import com.tastyhouse.domain.sms.port.SmsSender;
import com.tastyhouse.domain.sms.repository.SmsVerificationRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;
import com.tastyhouse.domain.shared.vo.VerificationCode;

/**
 * SMS 인증 발급·검증 규칙(도메인 서비스).
 *
 * <p>발급은 "같은 번호의 기존 미완료 인증을 모두 만료시킨 뒤 새 인증을 저장하고 발송한다"는,
 * 같은 애그리거트 타입의 여러 인스턴스를 한 트랜잭션에서 다루는 불변식이다(공통 지침 분류 C).
 * 검증(코드 대조 + 상태 전이 + 저장 + 이벤트 발행)도 web과 향후 ceo가 공유해야 하는 규칙이므로
 * 특정 액터의 command 서비스가 아니라 도메인 계층에 둔다.
 *
 * <p><b>발송이 발급에 포함되는 이유</b>: 인증코드는 발송되지 않으면 존재 가치가 없다. 과거에는
 * 발송 책임이 호출부에 흩어져 있어(비밀번호 재설정 파사드만 발송을 호출) 인증코드 발송 API가
 * 코드를 저장만 하고 발송하지 않는 버그가 있었다. {@link #issue}가 저장과 발송을 함께 수행하면
 * 그 누락이 구조적으로 불가능해지고, 발송 실패 시 인증 레코드가 롤백되어 "코드는 있는데 문자는
 * 오지 않은" 유령 레코드가 남지 않는다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 이벤트 발행은 Spring
 * {@code ApplicationEventPublisher}가 아니라 프레임워크-프리 포트인 {@link DomainEventPublisher}를
 * 통해 수행한다.
 */
public class SmsVerificationService {

    private final SmsVerificationRepository smsVerificationRepository;
    private final SmsSender smsSender;
    private final DomainEventPublisher domainEventPublisher;

    public SmsVerificationService(
        SmsVerificationRepository smsVerificationRepository,
        SmsSender smsSender,
        DomainEventPublisher domainEventPublisher
    ) {
        this.smsVerificationRepository = smsVerificationRepository;
        this.smsSender = smsSender;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 인증코드를 발급하고 SMS로 발송한다. 같은 휴대폰번호의 기존 미완료 인증은 모두 만료시킨다.
     *
     * <p>발송이 실패하면 예외가 전파되어 인증 레코드 저장도 함께 롤백된다(위 클래스 Javadoc 참고).
     */
    public SmsVerification issue(String phoneNumber) {
        smsVerificationRepository.expireAllPendingByPhoneNumber(phoneNumber);
        SmsVerification saved = smsVerificationRepository.save(SmsVerification.create(phoneNumber));

        smsSender.send(phoneNumber, SmsVerificationMessage.body(saved.getVerificationCode()));

        return saved;
    }

    /**
     * 발급된 인증코드를 검증하고 인증 완료 상태로 전이시킨다.
     *
     * <p>도메인이 프레임워크-프리라 더티 체킹이 없으므로 상태 전이 후 명시적으로 저장한다.
     * 검증 완료 후 {@link SmsVerifiedEvent}를 발행한다.
     */
    public void confirm(String phoneNumber, String verificationCode) {
        SmsVerification verification = smsVerificationRepository
            .findLatestPendingByPhoneNumber(phoneNumber, SmsVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        verification.verify(VerificationCode.of(verificationCode), now);
        smsVerificationRepository.save(verification);

        domainEventPublisher.publish(new SmsVerifiedEvent(
            verification.getSmsVerificationId(),
            phoneNumber,
            now
        ));
    }
}

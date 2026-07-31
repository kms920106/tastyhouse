package com.tastyhouse.domain.verification.domain.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.verification.domain.event.PhoneVerifiedEvent;
import com.tastyhouse.domain.verification.domain.model.PhoneVerification;
import com.tastyhouse.domain.verification.domain.model.PhoneVerificationStatus;
import com.tastyhouse.domain.verification.domain.repository.PhoneVerificationRepository;
import com.tastyhouse.domain.verification.domain.vo.VerificationCode;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 휴대폰 인증 발급·검증 규칙(도메인 서비스).
 *
 * <p>{@link EmailVerificationService}와 동일한 판정 근거로 도메인 계층에 둔다 — 발급은 "같은 번호의
 * 기존 미완료 인증을 모두 만료시킨 뒤 새 인증을 저장한다"는 같은 애그리거트 타입 다중 인스턴스
 * 불변식(공통 지침 분류 C)이고, 검증(코드 대조 + 상태 전이 + 저장 + 이벤트 발행)은 web과 향후 ceo가
 * 공유해야 하는 규칙이다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 이벤트 발행은 프레임워크-프리
 * 포트인 {@link DomainEventPublisher}를 통해 수행한다.
 */
public class PhoneVerificationService {

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PhoneVerificationService(
        PhoneVerificationRepository phoneVerificationRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.phoneVerificationRepository = phoneVerificationRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 인증코드를 발급한다. 같은 휴대폰번호의 기존 미완료 인증은 모두 만료시킨다.
     *
     * <p>발급된 코드 값을 발송 문구에 담아야 하는 호출부를 위해 저장된 인증 애그리거트를 반환한다.
     */
    public PhoneVerification issue(String phoneNumber) {
        phoneVerificationRepository.expireAllPendingByPhoneNumber(phoneNumber);
        return phoneVerificationRepository.save(PhoneVerification.create(phoneNumber));
    }

    /**
     * 발급된 인증코드를 검증하고 인증 완료 상태로 전이시킨다.
     *
     * <p>도메인이 프레임워크-프리라 더티 체킹이 없으므로 상태 전이 후 명시적으로 저장한다.
     * 검증 완료 후 {@link PhoneVerifiedEvent}를 발행한다.
     */
    public void confirm(String phoneNumber, String verificationCode) {
        PhoneVerification verification = phoneVerificationRepository
            .findLatestPendingByPhoneNumber(phoneNumber, PhoneVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        verification.verify(VerificationCode.of(verificationCode), now);
        phoneVerificationRepository.save(verification);

        domainEventPublisher.publish(new PhoneVerifiedEvent(
            verification.getPhoneVerificationId(),
            phoneNumber,
            now
        ));
    }
}

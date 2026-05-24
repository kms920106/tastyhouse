package com.tastyhouse.core.domain.verification.application;

import com.tastyhouse.core.domain.verification.application.dto.command.ConfirmPhoneVerificationCommand;
import com.tastyhouse.core.domain.verification.application.dto.command.SendPhoneVerificationCommand;
import com.tastyhouse.core.domain.verification.application.dto.result.PhoneVerificationResult;
import com.tastyhouse.core.domain.verification.domain.event.PhoneVerifiedEvent;
import com.tastyhouse.core.domain.verification.domain.model.PhoneVerification;
import com.tastyhouse.core.domain.verification.domain.model.PhoneVerificationStatus;
import com.tastyhouse.core.domain.verification.domain.repository.PhoneVerificationRepository;
import com.tastyhouse.core.domain.verification.domain.vo.VerificationCode;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationCommandService {

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void sendVerificationCode(SendPhoneVerificationCommand command) {
        phoneVerificationRepository.expireAllPendingByPhoneNumber(command.phoneNumber());

        phoneVerificationRepository.save(PhoneVerification.create(command.phoneNumber()));

//        smsSender.send(command.phoneNumber(), SMS_TEXT_TEMPLATE.formatted(verificationCode));
    }

    @Transactional
    public PhoneVerificationResult confirmVerificationCode(ConfirmPhoneVerificationCommand command) {
        PhoneVerification verification = phoneVerificationRepository
            .findLatestPendingByPhoneNumber(command.phoneNumber(), PhoneVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        verification.verify(new VerificationCode(command.verificationCode()), now);

        eventPublisher.publishEvent(new PhoneVerifiedEvent(
            verification.getPhoneVerificationId(),
            command.phoneNumber(),
            now
        ));

        log.info("휴대폰 인증 완료. phoneNumber={}", command.phoneNumber());

        return new PhoneVerificationResult(command.phoneNumber());
    }
}

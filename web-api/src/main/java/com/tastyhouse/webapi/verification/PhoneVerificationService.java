package com.tastyhouse.webapi.verification;

import com.tastyhouse.core.entity.verification.PhoneVerification;
import com.tastyhouse.core.entity.verification.PhoneVerificationStatus;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.PhoneVerificationCoreService;
import com.tastyhouse.external.sms.SmsSender;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneVerificationService {

    private static final String SMS_TEXT_TEMPLATE = "[TASTY HOUSE] 인증번호 [%s]를 입력해주세요.";

    private final PhoneVerificationCoreService phoneVerificationCoreService;
    private final SmsSender smsSender;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationCodeGenerator verificationCodeGenerator;

    @Transactional
    public void sendVerificationCode(String phoneNumber) {
        phoneVerificationCoreService.expireAllPendingByPhoneNumber(phoneNumber);

        String verificationCode = verificationCodeGenerator.generate();

        phoneVerificationCoreService.save(
            PhoneVerification.builder()
                .phoneNumber(phoneNumber)
                .verificationCode(verificationCode)
                .build()
        );

//        String smsText = SMS_TEXT_TEMPLATE.formatted(verificationCode);
//        smsSender.send(phoneNumber, smsText);
    }

    @Transactional
    public String confirmVerificationCode(String phoneNumber, String verificationCode) {
        PhoneVerification verification = phoneVerificationCoreService
            .findLatestPendingByPhoneNumber(phoneNumber, PhoneVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

        if (verification.isExpired()) {
            verification.expire();
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        if (!verification.getVerificationCode().equals(verificationCode)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }

        verification.verify();

        String phoneVerifyToken = jwtTokenProvider.createPhoneVerifyToken(phoneNumber);

        log.info("휴대폰 인증 완료. phoneNumber: {}", phoneNumber);

        return phoneVerifyToken;
    }

}

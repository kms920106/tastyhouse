package com.tastyhouse.domain.sms.service;

import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.shared.vo.VerificationCode;

final class SmsVerificationMessage {
    private static final String BODY_TEMPLATE = "[TASTY HOUSE] 인증번호 [%s]를 입력해주세요. (%d분 내 유효)";

    private SmsVerificationMessage() {
    }

    static String body(VerificationCode verificationCode) {
        return BODY_TEMPLATE.formatted(verificationCode.value(), SmsVerification.EXPIRATION_MINUTES);
    }
}

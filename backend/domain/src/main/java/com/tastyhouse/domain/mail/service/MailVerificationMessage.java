package com.tastyhouse.domain.mail.service;

import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationPurpose;
import com.tastyhouse.domain.shared.vo.VerificationCode;

final class MailVerificationMessage {
    private static final String SIGN_UP_SUBJECT = "[TASTY HOUSE] 회원가입 인증번호 안내";
    private static final String SIGN_UP_BODY_TEMPLATE =
        "[TASTY HOUSE] 회원가입 인증번호 [%s]를 입력해주세요. (%d분 내 유효)";

    private static final String PASSWORD_RESET_SUBJECT = "[TASTY HOUSE] 비밀번호 재설정 인증번호 안내";
    private static final String PASSWORD_RESET_BODY_TEMPLATE =
        "[TASTY HOUSE] 비밀번호 재설정 인증번호 [%s]를 입력해주세요. (%d분 내 유효)";

    private MailVerificationMessage() {
    }

    static String subject(MailVerificationPurpose purpose) {
        return switch (purpose) {
            case SIGN_UP -> SIGN_UP_SUBJECT;
            case PASSWORD_RESET -> PASSWORD_RESET_SUBJECT;
        };
    }

    static String body(MailVerificationPurpose purpose, VerificationCode verificationCode) {
        String template = switch (purpose) {
            case SIGN_UP -> SIGN_UP_BODY_TEMPLATE;
            case PASSWORD_RESET -> PASSWORD_RESET_BODY_TEMPLATE;
        };
        return template.formatted(verificationCode.value(), MailVerification.EXPIRATION_MINUTES);
    }
}

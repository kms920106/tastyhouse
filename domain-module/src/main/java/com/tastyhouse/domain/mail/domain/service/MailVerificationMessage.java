package com.tastyhouse.domain.mail.domain.service;

import com.tastyhouse.domain.mail.domain.model.MailVerification;
import com.tastyhouse.domain.mail.domain.model.MailVerificationPurpose;
import com.tastyhouse.domain.shared.vo.VerificationCode;

/**
 * 메일 인증 발송 문구.
 *
 * <p>문구가 도메인 정책이라 도메인이 소유한다 — 본문에 노출되는 유효시간이
 * {@code MailVerification}의 만료 정책과 같은 값이어야 하고, 발송 주체가 도메인 서비스이므로
 * 문구를 밖에 두면 파라미터로 주고받아야 한다. 과거에는 이 문구가 web-api
 * {@code AuthPasswordResetService}의 String 상수로 하드코딩되어 있어 재설정 흐름만 문구를 갖고
 * 회원가입 흐름은 발송 자체가 누락되어 있었다.
 *
 * <p>package-private으로 두어 {@link MailVerificationService} 외부에서 쓰이지 않게 한다.
 * i18n 요구가 생기면 도메인 포트({@code MailMessageResolver})로 승격하는 것이 정석 경로이며,
 * 지금은 Spring {@code MessageSource}를 쓸 수 없다(domain-module 프레임워크-프리).
 */
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

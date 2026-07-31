package com.tastyhouse.domain.mail.domain.model;

/**
 * 메일 인증 발급 목적.
 *
 * <p>발송 문구가 목적별로 다르므로({@code MailVerificationMessage}) 발급 시 목적을 함께 받는다.
 * 회원가입 인증과 비밀번호 재설정 인증은 코드 대조·만료 판정 규칙은 공유하지만 사용자에게
 * 보내는 문구는 달라야 한다.
 */
public enum MailVerificationPurpose {
    SIGN_UP, PASSWORD_RESET
}

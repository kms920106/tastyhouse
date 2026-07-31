package com.tastyhouse.domain.sms.domain.service;

import com.tastyhouse.domain.sms.domain.model.SmsVerification;
import com.tastyhouse.domain.shared.vo.VerificationCode;

/**
 * SMS 인증 발송 문구.
 *
 * <p>문구가 도메인 정책이라 도메인이 소유한다 — 본문에 노출되는 유효시간이
 * {@code SmsVerification}의 만료 정책과 같은 값이어야 하고, 발송 주체가 도메인 서비스이므로
 * 문구를 밖에 두면 파라미터로 주고받아야 한다. 소비 모듈(web/ceo)이 늘어날 때 문구가 복제되어
 * 갈라지는 것도 막는다.
 *
 * <p>package-private으로 두어 {@link SmsVerificationService} 외부에서 쓰이지 않게 한다.
 * i18n 요구가 생기면 도메인 포트({@code SmsMessageResolver})로 승격하는 것이 정석 경로이며,
 * 지금은 Spring {@code MessageSource}를 쓸 수 없다(domain-module 프레임워크-프리).
 */
final class SmsVerificationMessage {

    private static final String BODY_TEMPLATE = "[TASTY HOUSE] 인증번호 [%s]를 입력해주세요. (%d분 내 유효)";

    private SmsVerificationMessage() {
    }

    static String body(VerificationCode verificationCode) {
        return BODY_TEMPLATE.formatted(verificationCode.value(), SmsVerification.EXPIRATION_MINUTES);
    }
}

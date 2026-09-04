package com.tastyhouse.application.sms.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

/**
 * SMS 인증 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code SmsVerificationCommandService})을 알지 않는다. 도입
 * 근거는 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
@WebApp
public interface SmsVerificationCommandUseCase {

    void sendVerificationCode(SmsVerificationSendCommand command);

    /**
     * @return 인증이 완료된 휴대폰번호(컨트롤러가 이 값으로 smsVerifyToken을 발급한다)
     */
    String confirmVerificationCode(SmsVerificationConfirmCommand command);
}

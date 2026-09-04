package com.tastyhouse.webapplication.mail.port.in;

/**
 * 메일 인증 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code MailVerificationCommandService})을 알지 않는다. 도입
 * 근거는 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface MailVerificationCommandUseCase {

    void sendVerificationCode(MailVerificationSendCommand command);

    /**
     * @return 인증이 완료된 이메일 주소(컨트롤러가 이 값으로 mailVerifyToken을 발급한다)
     */
    String confirmVerificationCode(MailVerificationConfirmCommand command);
}

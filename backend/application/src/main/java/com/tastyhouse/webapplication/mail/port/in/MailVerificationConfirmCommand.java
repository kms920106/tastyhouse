package com.tastyhouse.webapplication.mail.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메일 인증번호 확인 command.
 *
 * <p>형식 검증은 Request의 jakarta.validation이 담당하고(400 계약·한국어 메시지 유지),
 * 이 record는 필수값 누락 같은 구조적 가드만 둔다.
 */
public record MailVerificationConfirmCommand(
    String email,
    String verificationCode
) {
    public MailVerificationConfirmCommand {
        if (email == null || verificationCode == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

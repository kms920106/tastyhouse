package com.tastyhouse.webapi.mail.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메일 인증번호 발송 command.
 *
 * <p>이메일 형식 검증은 Request의 jakarta.validation이 담당하고(400 계약·한국어 메시지 유지),
 * 이 record는 필수값 누락 같은 구조적 가드만 둔다.
 */
public record MailVerificationSendCommand(String email) {
    public MailVerificationSendCommand {
        if (email == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

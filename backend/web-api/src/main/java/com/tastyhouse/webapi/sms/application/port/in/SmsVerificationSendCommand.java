package com.tastyhouse.webapi.sms.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * SMS 인증번호 발송 command.
 *
 * <p>휴대폰번호 형식 검증은 Request의 jakarta.validation이 담당하고(400 계약·한국어 메시지 유지),
 * 이 record는 필수값 누락 같은 구조적 가드만 둔다.
 */
public record SmsVerificationSendCommand(String phoneNumber) {
    public SmsVerificationSendCommand {
        if (phoneNumber == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

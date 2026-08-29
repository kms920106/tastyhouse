package com.tastyhouse.adminapi.policy.application.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 약관 등록 command.
 *
 * <p>형식·길이 검증은 Request의 jakarta.validation이 담당하고(400 계약·한국어 메시지 유지),
 * 이 record는 필수값 누락 같은 구조적 가드만 둔다. 정책 유형 문자열의 enum 승격은 서비스가 맡는다.
 */
public record PolicyCreateCommand(
    String type,
    String version,
    String title,
    String content,
    boolean mandatory,
    LocalDateTime effectiveDate,
    String createdBy
) {
    public PolicyCreateCommand {
        if (type == null || version == null || title == null || content == null || effectiveDate == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

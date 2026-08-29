package com.tastyhouse.webapi.bug.application.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 버그 제보 등록 command.
 *
 * <p>형식·길이 검증은 Request의 jakarta.validation이 담당하고(400 계약·한국어 메시지 유지),
 * 이 record는 필수값 누락 같은 구조적 가드만 둔다.
 */
public record BugReportCreateCommand(
    Long reporterId,
    String device,
    String title,
    String content,
    String appVersion,
    String platform,
    String osVersion,
    List<Long> uploadedFileIds
) {
    public BugReportCreateCommand {
        if (reporterId == null || device == null || title == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

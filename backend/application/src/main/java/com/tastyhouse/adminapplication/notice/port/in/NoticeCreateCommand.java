package com.tastyhouse.adminapplication.notice.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 공지사항 등록 command.
 *
 * <p>형식·길이 검증은 Request의 jakarta.validation이 담당하고(400 계약·한국어 메시지 유지),
 * 이 record는 필수값 누락 같은 구조적 가드만 둔다.
 */
public record NoticeCreateCommand(
    String title,
    String content,
    boolean visible
) {
    public NoticeCreateCommand {
        if (title == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

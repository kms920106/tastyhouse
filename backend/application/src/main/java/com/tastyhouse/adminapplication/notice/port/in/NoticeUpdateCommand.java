package com.tastyhouse.adminapplication.notice.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 공지사항 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 */
public record NoticeUpdateCommand(
    Long noticeId,
    String title,
    String content,
    boolean visible
) {
    public NoticeUpdateCommand {
        if (noticeId == null || title == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

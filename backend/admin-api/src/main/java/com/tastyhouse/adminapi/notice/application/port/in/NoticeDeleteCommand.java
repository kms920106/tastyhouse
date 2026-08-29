package com.tastyhouse.adminapi.notice.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 공지사항 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record NoticeDeleteCommand(Long noticeId) {
    public NoticeDeleteCommand {
        if (noticeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static NoticeDeleteCommand of(Long noticeId) {
        return new NoticeDeleteCommand(noticeId);
    }
}

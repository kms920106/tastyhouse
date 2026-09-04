package com.tastyhouse.ceoapplication.ceo.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 자주 쓰는 문구 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toUpdateCommand}로 주입한다.
 */
public record CeoReplyPhraseUpdateCommand(
    Long ceoId,
    Long replyPhraseId,
    String name,
    String content
) {
    public CeoReplyPhraseUpdateCommand {
        if (ceoId == null || replyPhraseId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

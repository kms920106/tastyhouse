package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * FAQ 항목 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record FaqDeleteCommand(Long faqId) {
    public FaqDeleteCommand {
        if (faqId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static FaqDeleteCommand of(Long faqId) {
        return new FaqDeleteCommand(faqId);
    }
}

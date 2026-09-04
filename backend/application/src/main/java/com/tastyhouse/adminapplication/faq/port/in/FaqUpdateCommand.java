package com.tastyhouse.adminapplication.faq.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * FAQ 항목 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 */
public record FaqUpdateCommand(
    Long faqId,
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort,
    boolean visible
) {
    public FaqUpdateCommand {
        if (faqId == null || faqCategoryId == null || question == null || answer == null || sort == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

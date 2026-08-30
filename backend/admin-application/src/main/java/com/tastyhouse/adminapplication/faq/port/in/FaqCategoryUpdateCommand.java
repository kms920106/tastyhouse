package com.tastyhouse.adminapplication.faq.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * FAQ 카테고리 수정 command. 경로 변수 {@code categoryId}는 컨트롤러가 {@code toCommand(categoryId)}로 주입한다.
 */
public record FaqCategoryUpdateCommand(
    Long faqCategoryId,
    String name,
    Integer sort,
    boolean visible
) {
    public FaqCategoryUpdateCommand {
        if (faqCategoryId == null || name == null || sort == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

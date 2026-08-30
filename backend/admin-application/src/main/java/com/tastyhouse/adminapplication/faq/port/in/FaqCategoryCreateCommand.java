package com.tastyhouse.adminapplication.faq.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * FAQ 카테고리 등록 command.
 *
 * <p>형식·길이 검증은 Request의 jakarta.validation이 담당하고(400 계약·한국어 메시지 유지),
 * 이 record는 필수값 누락 같은 구조적 가드만 둔다.
 */
public record FaqCategoryCreateCommand(
    String name,
    Integer sort,
    boolean visible
) {
    public FaqCategoryCreateCommand {
        if (name == null || sort == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

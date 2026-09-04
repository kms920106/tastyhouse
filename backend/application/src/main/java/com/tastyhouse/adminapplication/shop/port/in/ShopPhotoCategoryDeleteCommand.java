package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 포토 카테고리 삭제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopPhotoCategoryDeleteCommand(
    Long categoryId
) {
    public ShopPhotoCategoryDeleteCommand {
        if (categoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopPhotoCategoryDeleteCommand of(Long categoryId) {
        return new ShopPhotoCategoryDeleteCommand(categoryId);
    }
}

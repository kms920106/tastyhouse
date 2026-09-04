package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 콘텐츠보드 삭제 command. 요청 본문이 없어 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopContentBoardDeleteCommand(
    Long contentBoardId
) {
    public ShopContentBoardDeleteCommand {
        if (contentBoardId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopContentBoardDeleteCommand of(Long contentBoardId) {
        return new ShopContentBoardDeleteCommand(contentBoardId);
    }}

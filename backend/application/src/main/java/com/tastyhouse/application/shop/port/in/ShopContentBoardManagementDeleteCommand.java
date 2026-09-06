package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopContentBoardManagementDeleteCommand(
    Long contentBoardId
) {
    public ShopContentBoardManagementDeleteCommand {
        if (contentBoardId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopContentBoardManagementDeleteCommand of(Long contentBoardId) {
        return new ShopContentBoardManagementDeleteCommand(contentBoardId);
    }}

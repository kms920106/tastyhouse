package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopContentBoardManagementDeleteCommand(
    Long contentBoardId
) {

    public ShopContentBoardManagementDeleteCommand {
        if (contentBoardId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopContentBoardManagementDeleteCommand of(Long contentBoardId) {
        return new ShopContentBoardManagementDeleteCommand(contentBoardId);
    }}

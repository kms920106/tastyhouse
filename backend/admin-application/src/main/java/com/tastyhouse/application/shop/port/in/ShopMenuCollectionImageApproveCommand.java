package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopMenuCollectionImageApproveCommand(
    Long imageId
) {

    public ShopMenuCollectionImageApproveCommand {
        if (imageId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopMenuCollectionImageApproveCommand of(Long imageId) {
        return new ShopMenuCollectionImageApproveCommand(imageId);
    }}

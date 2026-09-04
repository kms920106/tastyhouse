package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 메뉴판 이미지 승인 command. */
public record ShopMenuCollectionImageApproveCommand(
    Long imageId
) {
    public ShopMenuCollectionImageApproveCommand {
        if (imageId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopMenuCollectionImageApproveCommand of(Long imageId) {
        return new ShopMenuCollectionImageApproveCommand(imageId);
    }}

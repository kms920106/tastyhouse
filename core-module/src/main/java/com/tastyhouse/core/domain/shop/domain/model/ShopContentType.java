package com.tastyhouse.core.domain.shop.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ShopContentType {

    IMAGE("이미지"),
    GIF("GIF"),
    VIDEO("동영상");

    private final String description;

    public static ShopContentType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.SHOP_CONTENT_TYPE_UNKNOWN,
                ErrorCode.SHOP_CONTENT_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}

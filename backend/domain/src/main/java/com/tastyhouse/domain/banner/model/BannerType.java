package com.tastyhouse.domain.banner.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public enum BannerType {
    HOME, SIDEBAR;

    public static BannerType from(String code) {
        try {
            return valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BANNER_TYPE_UNKNOWN,
                ErrorCode.BANNER_TYPE_UNKNOWN.getDefaultMessage() + ": " + code);
        }
    }
}

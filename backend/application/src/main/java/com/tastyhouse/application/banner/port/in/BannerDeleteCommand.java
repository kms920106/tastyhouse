package com.tastyhouse.application.banner.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record BannerDeleteCommand(Long bannerId) {
    public BannerDeleteCommand {
        if (bannerId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static BannerDeleteCommand of(Long bannerId) {
        return new BannerDeleteCommand(bannerId);
    }
}

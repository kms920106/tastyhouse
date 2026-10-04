package com.tastyhouse.application.banner.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record BannerDeleteCommand(Long bannerId) {

    public BannerDeleteCommand {
        if (bannerId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static BannerDeleteCommand of(Long bannerId) {
        return new BannerDeleteCommand(bannerId);
    }
}

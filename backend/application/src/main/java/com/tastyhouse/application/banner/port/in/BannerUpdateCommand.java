package com.tastyhouse.application.banner.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record BannerUpdateCommand(
    Long bannerId,
    String type,
    String title,
    Long imageFileId,
    String linkUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer sort,
    boolean visible
) {
    public BannerUpdateCommand {
        if (bannerId == null || type == null || imageFileId == null || sort == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

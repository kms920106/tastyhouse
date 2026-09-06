package com.tastyhouse.application.banner.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record BannerCreateCommand(
    String type,
    String title,
    Long imageFileId,
    String linkUrl,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer sort,
    boolean visible
) {
    public BannerCreateCommand {
        if (type == null || imageFileId == null || sort == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

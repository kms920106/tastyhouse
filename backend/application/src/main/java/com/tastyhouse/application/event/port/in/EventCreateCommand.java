package com.tastyhouse.application.event.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record EventCreateCommand(
    String name,
    String description,
    String subtitle,
    Long thumbnailImageFileId,
    Long bannerImageFileId,
    String contentHtml,
    String status,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
    public EventCreateCommand {
        if (name == null || status == null || startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

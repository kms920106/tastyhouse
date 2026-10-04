package com.tastyhouse.application.event.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record EventUpdateCommand(
    Long eventId,
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

    public EventUpdateCommand {
        if (eventId == null || name == null || status == null || startAt == null || endAt == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

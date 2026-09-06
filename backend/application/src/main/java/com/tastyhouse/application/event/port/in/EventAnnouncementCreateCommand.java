package com.tastyhouse.application.event.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record EventAnnouncementCreateCommand(
    Long eventId,
    String name,
    String content,
    LocalDateTime announcedAt
) {
    public EventAnnouncementCreateCommand {
        if (eventId == null || name == null || content == null || announcedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

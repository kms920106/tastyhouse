package com.tastyhouse.application.event.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record EventAnnouncementUpdateCommand(
    Long eventId,
    String name,
    String content,
    LocalDateTime announcedAt
) {
    public EventAnnouncementUpdateCommand {
        if (eventId == null || name == null || content == null || announcedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}

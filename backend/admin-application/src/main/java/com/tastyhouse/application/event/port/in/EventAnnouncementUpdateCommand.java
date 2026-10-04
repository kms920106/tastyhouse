package com.tastyhouse.application.event.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record EventAnnouncementUpdateCommand(
    Long eventId,
    String name,
    String content,
    LocalDateTime announcedAt
) {

    public EventAnnouncementUpdateCommand {
        if (eventId == null || name == null || content == null || announcedAt == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}

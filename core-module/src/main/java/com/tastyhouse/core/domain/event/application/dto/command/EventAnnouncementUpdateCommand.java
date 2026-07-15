package com.tastyhouse.core.domain.event.application.dto.command;

import java.time.LocalDateTime;

public record EventAnnouncementUpdateCommand(
    String name,
    String content,
    LocalDateTime announcedAt
) {

    public static EventAnnouncementUpdateCommand of(
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        return new EventAnnouncementUpdateCommand(name, content, announcedAt);
    }
}

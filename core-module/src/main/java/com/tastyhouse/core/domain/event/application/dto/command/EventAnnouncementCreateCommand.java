package com.tastyhouse.core.domain.event.application.dto.command;

import java.time.LocalDateTime;

public record EventAnnouncementCreateCommand(
    String name,
    String content,
    LocalDateTime announcedAt
) {

    public static EventAnnouncementCreateCommand of(
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        return new EventAnnouncementCreateCommand(name, content, announcedAt);
    }
}

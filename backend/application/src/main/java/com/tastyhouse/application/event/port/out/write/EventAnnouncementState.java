package com.tastyhouse.application.event.port.out.write;

import java.time.LocalDateTime;

public record EventAnnouncementState(
    Long id,
    Long eventId,
    String name,
    String content,
    LocalDateTime announcedAt
) {
}

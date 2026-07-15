package com.tastyhouse.core.domain.event.application.dto.command;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.event.domain.model.EventStatus;

public record EventCreateCommand(
    String name,
    String description,
    String subtitle,
    Long thumbnailImageFileId,
    Long bannerImageFileId,
    String contentHtml,
    EventStatus status,
    LocalDateTime startAt,
    LocalDateTime endAt
) {

    public static EventCreateCommand of(
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
        String contentHtml,
        EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        return new EventCreateCommand(
            name, description, subtitle, thumbnailImageFileId, bannerImageFileId,
            contentHtml, status, startAt, endAt
        );
    }
}

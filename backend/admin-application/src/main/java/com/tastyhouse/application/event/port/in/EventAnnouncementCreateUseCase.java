package com.tastyhouse.application.event.port.in;

public interface EventAnnouncementCreateUseCase {

    Long createAnnouncement(EventAnnouncementCreateCommand command);
}

package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.event.port.out.EventAnnouncementResult;

public interface EventManagementAnnouncementQueryUseCase {

    EventAnnouncementResult getAnnouncement(Long id);
}

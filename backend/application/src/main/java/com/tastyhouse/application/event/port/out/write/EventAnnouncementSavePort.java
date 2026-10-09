package com.tastyhouse.application.event.port.out.write;

import com.tastyhouse.domain.event.model.EventAnnouncement;

public interface EventAnnouncementSavePort {

    EventAnnouncement save(EventAnnouncement eventAnnouncement);
}

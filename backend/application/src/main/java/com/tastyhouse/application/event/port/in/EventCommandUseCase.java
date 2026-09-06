package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface EventCommandUseCase {

    Long createEvent(EventCreateCommand command);

    void updateEvent(EventUpdateCommand command);

    void deleteEvent(EventDeleteCommand command);

    Long createAnnouncement(EventAnnouncementCreateCommand command);

    void updateAnnouncement(EventAnnouncementUpdateCommand command);

    Long createWinner(EventWinnerCreateCommand command);

    void deleteWinner(EventWinnerDeleteCommand command);
}

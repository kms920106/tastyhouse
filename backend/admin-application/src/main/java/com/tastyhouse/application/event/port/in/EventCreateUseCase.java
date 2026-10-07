package com.tastyhouse.application.event.port.in;

public interface EventCreateUseCase {

    Long createEvent(EventCreateCommand command);
}

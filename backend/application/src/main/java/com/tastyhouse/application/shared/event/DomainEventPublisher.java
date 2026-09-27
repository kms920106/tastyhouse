package com.tastyhouse.application.shared.event;

public interface DomainEventPublisher {
    void publish(Object event);
}

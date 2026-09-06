package com.tastyhouse.domain.shared.event;

public interface DomainEventPublisher {
    void publish(Object event);
}

package com.tastyhouse.testsupport.review.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.application.shared.event.DomainEventPublisher;

public class FakeDomainEventPublisher implements DomainEventPublisher {

    private final List<Object> publishedEvents = new ArrayList<>();

    @Override
    public void publish(Object event) {
        publishedEvents.add(event);
    }

    public List<Object> publishedEvents() {
        return List.copyOf(publishedEvents);
    }
}

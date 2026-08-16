package com.tastyhouse.domain.review.service;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 도메인 이벤트 발행 포트의 인메모리 fake. 발행된 이벤트를 그대로 쌓아 두어 테스트에서 검증할 수 있게 한다.
 */
public class FakeDomainEventPublisher implements DomainEventPublisher {

    private final List<Object> publishedEvents = new ArrayList<>();

    @Override
    public void publish(Object event) {
        publishedEvents.add(event);
    }
}

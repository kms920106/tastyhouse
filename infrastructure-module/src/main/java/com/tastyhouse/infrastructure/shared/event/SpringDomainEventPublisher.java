package com.tastyhouse.infrastructure.shared.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * {@link DomainEventPublisher} 포트의 Spring 어댑터.
 *
 * <p>Spring {@link ApplicationEventPublisher}에 위임해 도메인 이벤트를 발행한다.
 * {@code @TransactionalEventListener}/{@code @EventListener} 기반 리스너가 그대로 수신한다.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}

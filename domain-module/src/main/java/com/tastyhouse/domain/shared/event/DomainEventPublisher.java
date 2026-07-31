package com.tastyhouse.domain.shared.event;

/**
 * 도메인 이벤트 발행 포트.
 *
 * <p>도메인/애플리케이션 서비스가 Spring({@code ApplicationEventPublisher})에 직접 의존하지 않고
 * 이벤트를 발행할 수 있도록 하는 출력 포트다. 구현 어댑터는 infrastructure-module의
 * {@code SpringDomainEventPublisher}가 담당하며 Spring {@code ApplicationEventPublisher}에 위임한다.
 *
 * <p>이 포트를 도입해 core-module의 도메인 서비스를 POJO로 하강시킬 때 Spring 이벤트 인프라 의존을
 * 제거할 수 있다. 이후 각 도메인 작업에서 core application 계층의 {@code ApplicationEventPublisher}
 * 사용처를 이 포트로 대체한다.
 */
public interface DomainEventPublisher {

    void publish(Object event);
}

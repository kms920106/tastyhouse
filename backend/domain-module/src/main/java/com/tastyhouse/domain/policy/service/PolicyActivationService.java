package com.tastyhouse.domain.policy.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.policy.event.PolicyActivatedEvent;
import com.tastyhouse.domain.policy.model.PolicyDocument;
import com.tastyhouse.domain.policy.repository.PolicyDocumentRepository;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * 정책 활성화 규칙(도메인 서비스).
 *
 * <p>"같은 유형의 정책은 동시에 하나만 현행(current)일 수 있다"는 규칙은 활성화 대상 정책 하나만으로
 * 판단할 수 없다 — 같은 유형의 기존 현행 정책을 함께 비활성화해야 하므로 한 트랜잭션에서 같은
 * 애그리거트 타입의 두 인스턴스를 load &amp; save 한다(공통 지침 분류 C). 특정 액터(admin)에 묶이지
 * 않는 도메인 불변식이라 소비 모듈의 command 서비스가 아니라 도메인 계층에 두어 여러 모듈에서
 * 재사용해도 규칙이 갈리지 않게 한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code DomainServiceConfig}가 담당한다. 호출자(command 서비스)의 트랜잭션
 * 안에서 실행된다. 이벤트 발행은 Spring {@code ApplicationEventPublisher}가 아니라 프레임워크-프리
 * 포트인 {@link DomainEventPublisher}를 통해 수행한다.
 */
public class PolicyActivationService {

    private final PolicyDocumentRepository policyDocumentRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PolicyActivationService(
        PolicyDocumentRepository policyDocumentRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.policyDocumentRepository = policyDocumentRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 지정된 정책을 현행으로 전이시키고, 같은 유형의 기존 현행 정책은 비활성화한다.
     *
     * <p>도메인이 프레임워크-프리라 더티 체킹이 없으므로 두 인스턴스 모두 명시적으로 저장한다.
     * 활성화 완료 후 {@link PolicyActivatedEvent}를 발행한다.
     */
    public void activate(PolicyDocument newPolicy) {
        policyDocumentRepository.findCurrentEntityByType(newPolicy.getType())
            .ifPresent(current -> {
                current.deactivate();
                policyDocumentRepository.save(current);
            });

        newPolicy.activate();
        policyDocumentRepository.save(newPolicy);

        domainEventPublisher.publish(new PolicyActivatedEvent(
            newPolicy.getPolicyDocumentId(),
            newPolicy.getType(),
            newPolicy.getVersion(),
            LocalDateTime.now()
        ));
    }
}

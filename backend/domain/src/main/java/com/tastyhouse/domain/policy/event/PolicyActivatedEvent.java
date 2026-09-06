package com.tastyhouse.domain.policy.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;

/**
 * 약관·정책 문서가 현행으로 전이됐음을 알리는 도메인 이벤트.
 *
 * <p>소비처는 {@code infrastructure/policy/listener/PolicyActivatedEventListener}이며, 지금은 전이
 * 사실만 기록한다. 약관이 개정되면 전체 회원에게 재동의 요청·개정 고지를 보내야 할 수 있고(전자상거래법상
 * 고지 의무), 그 후처리는 정책 활성화 트랜잭션과 분리된 비동기 작업이어야 하므로 이 이벤트가 그 접점이
 * 된다 — 다만 발송 대상이 전체 회원이라 배치·큐 설계가 선행돼야 해서 아직 붙이지 않았다.
 */
public record PolicyActivatedEvent(
    PolicyDocumentId policyDocumentId,
    PolicyType type,
    String version,
    LocalDateTime activatedAt
) {}

package com.tastyhouse.domain.policy.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;

/**
 * 약관·정책 문서가 현행으로 전이됐음을 알리는 도메인 이벤트.
 *
 * <p><b>현재 리스너 없음 — 의도된 발행이다.</b> P9(도메인 이벤트 정비)에서 수신자 없는 발행 7종을
 * 검토할 때, 이 이벤트만은 소비 수요가 실재할 가능성이 높다고 판단해 남겼다. 약관이 개정되면 전체
 * 회원에게 재동의 요청·개정 고지를 보내야 할 수 있고(전자상거래법상 고지 의무), 그 후처리는 정책
 * 활성화 트랜잭션과 분리된 비동기 작업이어야 하므로 이 이벤트가 그 접점이 된다.
 *
 * <p>따라서 "리스너가 없으니 죽은 코드"로 보고 다시 제거 대상에 올리지 않는다. 소비처를 만들 때는
 * {@code infrastructure/policy/listener/}에 {@code @TransactionalEventListener(AFTER_COMMIT)}로 추가한다.
 */
public record PolicyActivatedEvent(
    PolicyDocumentId policyDocumentId,
    PolicyType type,
    String version,
    LocalDateTime activatedAt
) {}

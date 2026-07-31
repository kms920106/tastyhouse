# P9. 도메인 이벤트 정비 — 수신자 없는 발행 7종 처리 + 발행 규율

## 배경

이벤트 record 22종이 `DomainEventPublisher` 포트로 전부 실제 발행되고(28개 발행 지점), 구독 15종은 `@TransactionalEventListener(AFTER_COMMIT)` 규약대로 동작한다 — 구조는 정석. 문제는 (1) 7종(32%)은 리스너가 없어 사실상 no-op 발행이고, (2) 발행이 전부 도메인 서비스에서 일어나 "전이했는데 발행 잊음"이 구조적으로 안 막히며, (3) 중복 발행 가능 지점이 있다는 것.

## 문제 상세

**리스너 없는 발행 전용 이벤트 7종**:

| 이벤트 | 발행 지점 |
|---|---|
| `OrderCreatedEvent` | `OrderPlacementService.java:201` |
| `RefundRequestedEvent` | `PaymentCancellationService.java:143` |
| `ProductCreatedEvent` | `ProductRegistrationService.java:102` |
| `ProductSoldOutChangedEvent` | `ProductRegistrationService.java:151` |
| `ProductDeactivatedEvent` | `ProductRegistrationService.java:166` |
| `ReviewLikedEvent` | `ReviewLifecycleService.java:222` |
| `PolicyActivatedEvent` | `PolicyActivationService.java:53` |

**기타**:
- `Order`는 생성 시에만 이벤트가 있고 `confirm()`/`cancel()` 전이 이벤트가 없음(`OrderTransitionService:100-111`).
- `PointLedgerService.java:113,124` — `PointUsedEvent`를 서로 다른 두 메서드에서 각각 발행. 한 트랜잭션에서 두 경로가 함께 타면 중복 발행 가능성(확정 필요).

## 작업 지시

1. **7종 각각에 대해 "소비처를 만들 것인가, 발행을 제거할 것인가"를 사용자에게 체크리스트로 질문**한다. 질문 전에 각 이벤트의 원래 의도(Javadoc·발행 지점 문맥)를 조사해 선택지에 근거를 첨부한다. 예시 형식:
   > - [ ] `OrderCreatedEvent` — (a) 유지+향후 소비 예정(주석으로 용도 명시) / (b) 발행 제거 / (c) 지금 소비처 구현(무엇을: ___)
2. "발행 제거" 결정분은 이벤트 record·발행 지점·관련 테스트를 함께 삭제한다(죽은 코드 잔류 금지). "유지" 결정분은 이벤트 Javadoc에 "현재 리스너 없음 — 의도된 발행(사유)"를 명시해 다음 조사자가 재발견하지 않게 한다.
3. `PointUsedEvent` 중복 발행 가능성 확정: `PointLedgerService.java:113,124` 두 발행 지점이 한 트랜잭션에서 함께 실행되는 호출 경로가 실제로 있는지 호출부를 역추적한다. 있으면 발행 지점을 단일화, 없으면 "경로상 배타적"임을 주석으로 남긴다.
4. `Order` 전이 이벤트(`OrderConfirmedEvent`/`OrderCancelledEvent`) 신설 여부는 소비 수요가 있을 때만 — 이번엔 만들지 않는다(YAGNI). 단 P1(상태 머신)이 끝난 뒤 전이 지점이 정리되므로, 필요해지면 그 지점에 발행을 추가하기 쉽다는 메모만 남긴다.
5. (선택 조사) 애그리거트 이벤트 축적 패턴(`pullDomainEvents()`) 도입은 **이번 범위 제외** — 전 애그리거트 구조 변경이라 별도 설계 결정. 조사 의견만 보고서에 첨부.

## 수용 기준

- [ ] 7종 각각의 처리 방침이 사용자 결정으로 확정되고 실행됨
- [ ] "유지" 이벤트는 Javadoc에 리스너 부재가 의도임이 명시됨, "제거" 이벤트는 record·발행부·테스트까지 완전 삭제
- [ ] `PointUsedEvent` 중복 발행 가능성이 확정(수정 또는 배타성 주석)
- [ ] 구독 중인 15종의 동작 무변경
- [ ] 테스트 통과 (verify-without-gradle)

## 주의사항

- **P1(Order 상태 머신)과 `OrderTransitionService` 파일이 겹칠 수 있다** — P1 완료 후 착수 권장.
- `DomainEventPublisher`는 domain-module의 프레임워크-프리 포트, 어댑터는 `infrastructure/shared/event/SpringDomainEventPublisher` — 이 구조는 건드리지 않는다.
- 발행 제거는 되돌리기 쉬우나, 외부 시스템(로그 수집 등)이 이벤트를 소비하고 있지 않은지 infrastructure listener 외의 소비 경로(로깅 AOP 등)도 확인 후 제거할 것.

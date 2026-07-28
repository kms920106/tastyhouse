# reservation 도메인 전환

> 선행: `tasks/README.md` + `00-phase0`(특히 낙관적 락 예외 번역) + 그룹 1 완료. 그룹 2(병렬 가능).

## 현황
- core: `reservation/application/` — `ReservationCommandService`(2 repo: Reservation+ReservationSlot, 명시적 save — confirm/reject/complete/cancel + releaseSlot), `ReservationCreator`(낙관적 락 재시도 루프), QueryService + `ReservationResult`. `ReservationSlot`은 `@Version` 보유.
- 소비자: web-api(예약 생성·취소·confirm/reject/complete — 점주 흐름 포함 여부 확인), ceo-api(예약주문 설정 연관 여부 확인).

## 작업
1. **(C) 하강 — 이 도메인의 핵심**: `ReservationCreator`의 "슬롯 정원 검증→차감→예약 생성, 낙관적 락/유니크 충돌 재시도"는 전형적 액터 무관 불변식 → `domain/service/ReservationBookingService`(가칭)로 하강. 재시도 판별은 Phase 0의 `OptimisticLockConflictException`(shared)으로 교체(spring-orm 예외 직접 참조 금지). 취소 시 `releaseSlot`(예약 상태전이 + 슬롯 반납 원자 연산)도 함께 하강.
2. **(A)**: confirm/reject/complete 등 단일 애그리거트 상태전이는 web(또는 ceo) `ReservationCommandService`(@Transactional)로 흡수(패턴 2, 명시적 save 유지). `SlotPolicy` 상수 유틸은 `domain/service/` 인접 배치.
3. **(B)**: infra `infrastructure/reservation/query/ReservationQueryDao` 신설(패턴 3 — 내 예약 목록/상세), `ReservationResult`는 infra query 패키지로 이관. web `ReservationQueryService`(readOnly)가 DAO 주입. Repository 2개 write 순수화(패턴 4).
4. core `reservation/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시. 낙관적 락 재시도 동작(예외 번역 경유)이 유지됨을 코드 경로로 확인해 기록.

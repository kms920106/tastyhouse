# point 도메인 전환

> 선행: `tasks/README.md` + `00-phase0` + 그룹 1 완료. 그룹 2(병렬 가능). **이벤트 리스너 보유 — order/payment 클러스터와 결합.**

## 현황
- core: `point/application/` — `PointCommandService`(2 repo: Point+PointHistory, 이벤트 발행, 명시적 save 5지점), `PointEventListener`(주문/결제 이벤트에 반응해 적립·회수 추정), QueryService + `PointHistoryResult`.
- 소비자: web-api(내 포인트 조회 `PointApiController`), admin-api(포인트 관리), 이벤트 경유 간접 소비(order/payment).

## 작업
1. **(C) 하강 — 이 도메인의 핵심**: `usePoints`/`earnPoints`/`refundPoints`/`reclaimEarnedPoints`/`deductPoints`는 전부 "Point 잔액 변경 + PointHistory 기록"의 원자 연산이자 이벤트로 트리거되는 액터 무관 규칙 → `PointLedgerService`(가칭) 도메인 서비스로 통째 하강(패턴 1). `DomainServiceConfig` 등록.
2. **(E)**: `PointEventListener` → infrastructure `point/listener/`, 본문은 `PointLedgerService` 호출로 축소.
3. **(A)**: admin의 수동 적립/차감이 있으면 admin `PointCommandService`(@Transactional)에서 `PointLedgerService` 호출(패턴 2).
4. **(B)**: infra `infrastructure/point/query/PointQueryDao` 신설(패턴 3 — 잔액·이력 페이징), `PointHistoryResult`는 infra query 패키지로 이관. web `PointQueryService`(readOnly)가 DAO 주입. Repository write 순수화(패턴 4).
5. core `point/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시. `PointLedgerService`가 40-order/41-payment 작업의 호출 대상이 되므로 시그니처를 이 파일 하단에 기록해 인계.

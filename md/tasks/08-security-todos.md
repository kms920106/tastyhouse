# 작업지시서 08 — 예약(Reservation) 도메인 소유권 검증 TODO 해소 (보안)

## 배경 (왜)

**이 작업지시서는 다른 구조 개선 작업들과 달리 실제 보안 취약점을 다룬다.** 예약 상태 전이(승인/거절/완료) API에 "점주 본인 검증"이 구현되어 있지 않다는 TODO가 코드와 컨트롤러 양쪽에 남아 있다. 현재 상태로는 **다른 점주의 예약을 조작할 수 있는 권한 검증 누락(Broken Access Control, OWASP Top 10 A01)**일 가능성이 있다. 우선순위 표에서는 "후순위"로 분류했지만, 이는 구조 개선 대비 순서일 뿐 보안 심각도상으로는 조기에 처리하는 것을 권장한다.

## 현재 상태 (근거)

TODO가 남아 있는 정확한 위치:

- `core-module/src/main/java/com/tastyhouse/core/domain/reservation/application/ReservationCommandService.java:57` — "TODO(보안)" 주석
- `core-module/src/main/java/com/tastyhouse/core/domain/reservation/application/ReservationCommandService.java:69` — "TODO(보안)" 주석
- `core-module/src/main/java/com/tastyhouse/core/domain/reservation/application/ReservationCommandService.java:82` — "TODO(보안)" 주석
- `web-api/src/main/java/com/tastyhouse/webapi/reservation/ReservationApiController.java:109` — "TODO(보안): Shop-owner 연결 후 점주 본인 검증 추가 필요"
- `web-api/src/main/java/com/tastyhouse/webapi/reservation/ReservationApiController.java:117` — 동일 취지
- `web-api/src/main/java/com/tastyhouse/webapi/reservation/ReservationApiController.java:125` — 동일 취지
- `web-api/src/main/java/com/tastyhouse/webapi/reservation/ReservationApiController.java:133` — 동일 취지

TODO 문구("Shop-owner 연결 후")로 보아, 이 검증이 미구현인 이유는 **점주(shop owner)와 매장(shop) 간의 연결 관계가 아직 도메인 모델에 존재하지 않았기 때문**으로 추정된다. 이 전제가 지금도 유효한지 먼저 확인해야 한다.

관련 기존 자원:
- `core-module`에 이미 `AccessDeniedException`(`core/exception/AccessDeniedException.java`, `BusinessException` 상속)이 존재 — 권한 없는 접근에 사용하는 정식 예외 타입.
- `ReservationCommandService`가 다루는 대상 메서드는 confirm/reject/complete(및 cancel일 가능성) — `루트 CLAUDE.md`의 "도메인 모델 / JPA 엔티티 분리 규칙" reservation 항목에서 이 4개 메서드에 이미 명시적 `save` 호출이 추가되었다는 기록이 있어, 최근에 손을 댄 이력이 있는 메서드들이다.

## 작업 지시

### 8-1. 전제조건 재확인

1. `shop` 도메인에 점주(owner) 계정과 매장을 연결하는 관계가 현재 존재하는지 확인한다. `admin` 도메인의 `Admin` 엔티티가 점주 역할을 겸하는지, 별도 `ShopOwner` 개념이 있는지, 아니면 `member` 도메인의 특정 역할(role)로 표현되는지 확인.
2. 만약 이 연결 관계가 여전히 존재하지 않는다면, 이 TODO는 **더 큰 선행 작업(점주 계정 체계 설계)이 필요한 것**이므로 이 작업지시서 범위를 벗어난다 — 그 사실을 확인하고 별도 작업지시서로 승격할 것을 제안한다.
3. 연결 관계가 이미 존재한다면(예: 최근 다른 작업에서 추가되었을 수 있음), 8-2로 진행한다.

### 8-2. 소유권 검증 구현

1. `ReservationCommandService`의 confirm/reject/complete(및 관련 메서드) 각각에, 요청자(현재 인증된 사용자)의 ID가 해당 예약이 속한 매장의 소유주 ID와 일치하는지 검증하는 로직을 추가한다.
2. 불일치 시 `AccessDeniedException`(`core/exception`)을 던진다.
3. `ReservationCommandService`의 메서드 시그니처에 요청자 식별자(예: 점주의 `MemberId` 또는 `AdminId`)가 필요한 경우, 이를 파라미터로 추가한다 — ID VO 경계 규칙(HTTP 경계는 `Long`, core 서비스는 VO)을 따른다.
4. `web-api/.../ReservationApiController.java`의 4개 지점에서, 현재 인증 컨텍스트(Spring Security의 `Authentication` 등 이 프로젝트가 쓰는 방식)로부터 요청자 식별자를 꺼내 Service 호출 시 전달하도록 수정한다.
5. TODO 주석을 전부 제거한다.

## 완료 기준

- [ ] shop-owner 연결 관계의 현재 존재 여부가 확인됨
- [ ] (존재할 경우) `ReservationCommandService`의 4개 지점에 소유권 검증이 추가되고 `AccessDeniedException`을 던짐
- [ ] `ReservationApiController`의 4개 지점에서 인증 컨텍스트로부터 요청자 식별자를 전달함
- [ ] 7개 TODO 주석이 전부 제거됨
- [ ] (연결 관계가 없을 경우) 선행 작업 필요성이 명시적으로 문서화되고 별도 작업으로 분리 제안됨

## 주의사항

- **이것은 보안 이슈이므로 "동작하는 것처럼 보이지만 검증이 없는" 상태로 방치하지 않는다.** 전제조건이 충족되지 않아 지금 구현이 불가능하다면, 최소한 이 사실과 리스크를 명확히 남겨야 한다.
- 검증 로직 추가 시 기존 정상 흐름(점주 본인이 자기 매장 예약을 처리하는 경우)이 깨지지 않는지 반드시 확인.
- 이 작업은 다른 어떤 작업지시서에도 의존하지 않으며, 보안 심각도상 일정이 맞으면 가장 먼저 처리하는 것을 권장한다.
- NO_COMMIT_OR_ROLLBACK — 추천 커밋 메시지: `fix(reservation): 예약 상태 전이 시 점주 본인 소유권 검증 추가`.

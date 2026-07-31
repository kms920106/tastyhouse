# P6. infrastructure에 하드코딩·복제된 도메인 규칙 회수

## 배경

도메인 규칙(비즈니스 정책)이 infrastructure-module의 query DAO/RepositoryImpl에 리터럴로 박혀 있고, 한 규칙이 두 파일에 복제된 사례도 있다. 한쪽만 고치면 화면 표시와 실제 차단 로직이 갈라지는 잠복 버그 구조다.

## 문제 상세

1. **`BLOCKING_STATUSES` 복제 (가장 위험)**: "회원당 1일 1예약" 차단 대상 상태 `[PENDING, CONFIRMED, COMPLETED]`가 두 곳에 리터럴로 존재:
   - `infrastructure-module/.../reservation/query/ReservationQueryDao.java:48-52`
   - `infrastructure-module/.../reservation/persistence/ReservationRepositoryImpl.java:38`
   `ReservationStatus` enum에 `isBlocking()` 같은 판정이 없어서 규칙이 인프라에 두 번 산다.
2. **정렬 정책 문자열 리터럴**: `infrastructure-module/.../review/query/ReviewQueryDao.java:282-292, 376-386` — `if ("RECOMMENDED".equals(sortType)) ... else if ("OLDEST".equals(sortType))`. 도메인 enum 경계 규칙(String→enum 승격은 Service에서)이 read 경로에는 미적용 — 오타 정렬값이 조용히 기본 정렬로 빠진다.
3. **정책 상수가 DAO 소유**:
   - `infrastructure-module/.../shop/query/ShopSearchQueryDao.java:57-58` — `MAP_MARKER_RADIUS_METERS = 200.0`, `METERS_PER_DEGREE = 111000.0`
   - `infrastructure-module/.../shop/query/ShopChoiceQueryDao.java:54` — `EDITOR_CHOICE_PRODUCT_LIMIT = 2`

## 작업 지시

1. `domain-module/.../reservation/domain/model/ReservationStatus.java`(위치 확인 후)에 `isBlocking()` 판정(또는 `static Set<ReservationStatus> blockingStatuses()`)을 추가하고, 위 두 infra 파일이 이를 참조하도록 교체한다. 리터럴 목록 제거. Javadoc에 "회원당 1일 1예약 차단 판정의 단일 원천"임을 명시.
2. 리뷰 정렬: 도메인 enum(예: `ReviewSortType` — RECOMMENDED/LATEST/OLDEST, 실제 후보값은 컨트롤러/Request의 allowableValues에서 확인)을 domain-module `review/domain/model/`에 신설하고 `from(String)` 팩토리(실패 시 `BusinessException(ErrorCode.XXX_UNKNOWN)`, CLAUDE.md 도메인 enum 경계 규칙 형식)를 둔다. web-api `ReviewQueryService`에서 String→enum 승격 후 DAO에는 enum을 전달, DAO의 문자열 분기를 enum switch로 교체.
   - 단, query DAO 계층은 "표현 목적 조회는 VO를 쓰지 않는다"(ID의 경우 Long) 규칙이 있다 — **enum은 ID VO와 달리 도메인 어휘이므로 DAO 파라미터로 허용할지, 아니면 DAO는 정렬 전략을 내부 판정하지 않도록 `OrderSpecifier`를 서비스가 주지 않는 현 구조를 유지한 채 enum만 받을지** 판단이 필요하다. 기존 코드에서 DAO가 도메인 enum을 파라미터로 받는 선례(`ReservationStatus` 등)를 grep으로 확인해 다수파를 따르고, 결정 근거를 보고에 남긴다.
3. 정책 상수 이동: 에디터초이스 개수 제한(`EDITOR_CHOICE_PRODUCT_LIMIT`)은 도메인 정책이므로 domain-module의 정책 객체(선례: `reservation/domain/service/SlotPolicy.java`)로 이동. 지도 반경·미터 환산 상수는 지리 계산 구현 세부이므로 DAO 잔류가 정당한지 판정하고 — 잔류 시 왜 도메인 규칙이 아닌지 주석 한 줄 추가.
4. 다른 DAO에도 유사 사례가 있는지 전수 grep: `infrastructure-module`의 `*QueryDao.java`/`*RepositoryImpl.java`에서 상태 리터럴 목록·문자열 정책 분기·업무 상수를 찾아 같은 기준으로 처리 목록화(발견분은 보고에 포함, 수정 여부는 규모에 따라 판단).

## 수용 기준

- [ ] `BLOCKING_STATUSES` 리터럴이 infra에서 사라지고 단일 원천(`ReservationStatus`)만 존재
- [ ] 리뷰 정렬 문자열 분기가 enum 기반으로 교체, 미지원 값은 400 `BusinessException`
- [ ] `EDITOR_CHOICE_PRODUCT_LIMIT`가 도메인 정책 위치로 이동
- [ ] 동작 무변경(정렬 결과·차단 판정·초이스 개수 동일) — 대표 케이스 대조
- [ ] 테스트 통과 (verify-without-gradle)

## 주의사항

- 정렬 enum 신설 시 Swagger `allowableValues`(Request/@Parameter)와 후보값을 일치시킬 것.
- 미지원 정렬값이 현재는 조용히 기본 정렬로 빠진다 — enum 승격 후 400이 되면 **동작 변경**이다. 기존 프론트가 보내는 값을 확인하고, 위험하면 `from()`에서 기본값 폴백을 선택할 수 있게 근거와 함께 결정(결정을 보고에 명시).

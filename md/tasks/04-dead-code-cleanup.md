# 작업지시서 04 — 죽은 코드·불완전 코드 정리

## 배경 (왜)

전수 조사 중 두 가지 "완성되지 않았거나 더 이상 필요 없어 보이는" 코드가 발견되었다. 둘 다 삭제/완성 여부를 판단하려면 실제 호출부를 확인해야 하므로, 코드 이해 부담이 낮은 grep 기반 작업으로 분류했다.

## 현재 상태 (근거)

### 1. `ShopOwnerMessageHistory` — JpaRepository 누락

`infrastructure-module/src/main/java/com/tastyhouse/infrastructure/shop/persistence/` 하위에 `ShopOwnerMessageHistoryJpaEntity`와 대응 `Mapper`는 존재하지만, 다른 모든 shop 하위 엔티티(예: `ShopAmenity`, `ShopBusinessHour`, `ShopBookmark` 등 15개)와 달리 **`ShopOwnerMessageHistoryJpaRepository`가 없다**. `RepositoryImpl`도 없을 가능성이 높다(JpaRepository가 없으면 만들 수 없음).

이는 다음 중 하나를 의미한다:
- (a) 이 엔티티를 쓰는 기능이 아직 core-module의 application 계층에 구현되지 않은 미완성 상태(JpaEntity/Mapper만 먼저 만들어두고 나머지는 미착수)
- (b) 애초에 이 엔티티를 참조하는 도메인 로직이 삭제되었는데 persistence 어댑터만 남은 죽은 코드

### 2. `ReviewCommandService`의 임시 직접 참조 TODO

`core-module/src/main/java/com/tastyhouse/core/domain/review/application/ReviewCommandService.java:54`에 다음 취지의 TODO가 있다: "Shop BC가 아직 마이그레이션되지 않아 임시로 직접 참조"라는 내용.

이 TODO가 작성된 시점에는 `shop` 도메인이 아직 core-module에서 JPA 엔티티로 남아 있었을 가능성이 있다. 하지만 현재 조사로 **`shop` 도메인은 이미 `infrastructure-module`로 완전히 이전 완료**된 상태임이 확인되었다(루트 `CLAUDE.md`의 "도메인 모델 / JPA 엔티티 분리 규칙" 절, `shop` 도메인 항목 참고). 즉 이 TODO가 가리키는 전제조건은 이미 해소되었을 가능성이 높다.

## 작업 지시

### 4-1. ShopOwnerMessageHistory 조사

1. `core-module` 전체에서 `ShopOwnerMessageHistory`(또는 관련 도메인 모델명)를 참조하는 코드를 grep으로 찾는다.
2. 호출부가 있다면: 누락된 `JpaRepository`와 `RepositoryImpl`을 다른 shop 하위 엔티티 패턴에 맞춰 생성해 완성한다.
3. 호출부가 없다면: `ShopOwnerMessageHistoryJpaEntity`, 대응 `Mapper`, 그리고 core-module에 남아 있는 관련 도메인 모델(있다면)을 전부 삭제한다. "사용하지 않는 것으로 확인되면 완전히 삭제한다"는 원칙(루트 CLAUDE.md 상단 공통 지침)을 따른다.

### 4-2. ReviewCommandService TODO 해소 확인

1. `core-module/.../review/application/ReviewCommandService.java:54` 주변 코드를 읽고, 어떤 도메인(추정: `shop`)을 어떤 방식으로 "임시 직접 참조"하고 있는지 정확히 파악한다.
2. 그 참조 방식이 현재 `shop` 도메인의 정식 접근 경로(`ShopRepository` 인터페이스, `ShopId` VO 등)로 대체 가능한지 확인한다.
3. 대체 가능하면 정식 경로로 교체하고 TODO 주석을 제거한다. 대체가 아직 불가능한 다른 이유가 있다면(예: 순환 참조, 다른 미완료 작업 의존), 그 이유를 TODO 대신 명확한 코멘트로 남기거나 별도 이슈로 분리한다.

## 완료 기준

- [ ] `ShopOwnerMessageHistory`의 호출부 존재 여부가 확인되고, 그에 따라 완성 또는 삭제됨
- [ ] `ReviewCommandService.java:54`의 TODO가 해소되거나(정식 참조로 교체), 해소 불가 이유가 명확히 문서화됨
- [ ] 삭제 결정 시 관련 도메인 모델·매퍼·엔티티·리포지토리 인터페이스까지 전부 일관되게 제거됨(부분 삭제로 인한 컴파일 에러 없음)

## 주의사항

- 삭제 전 반드시 grep으로 참조 여부를 확인한다. 리플렉션이나 QueryDSL 문자열 참조로 간접 사용될 가능성도 배제하지 말 것(작업지시서 02의 PathBuilder 우회처럼 문자열 기반 참조가 있을 수 있음).
- `ReviewCommandService` 수정은 리뷰 도메인의 다른 로직에 영향을 주지 않는 국소적 변경이어야 한다.
- gradle build 테스트는 진행하지 않는다 — grep과 코드 리딩으로 검증.
- NO_COMMIT_OR_ROLLBACK — 추천 커밋 메시지: `refactor(shop): 미사용 ShopOwnerMessageHistory 정리` 및/또는 `refactor(review): Shop 도메인 임시 직접 참조를 정식 경로로 교체` (실제 조사 결과에 따라 둘 중 하나 또는 둘 다 별도 커밋으로 분리 권장).

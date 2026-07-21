# 작업지시서 02 — PathBuilder 문자열 우회를 정식 Q타입 조인으로 복원

## 배경 (왜)

`core-module`을 100% JPA-free로 전환하는 과정에서, 도메인들이 순차적으로 `core-module`의 순수 POJO 모델에서 `infrastructure-module`의 JPA 엔티티로 이동했다. 이 이동이 **한 번에 전체가 아니라 도메인별로 순차적으로** 진행되었기 때문에, 특정 시점에는 이미 이동한 도메인(A)의 리포지토리 구현체가 아직 core에 남아 있는 다른 도메인(B)의 JPA 엔티티 Q타입을 import할 수 없는 상황이 발생했다(core는 infrastructure를 의존하지 않으므로 그 패키지를 컴파일 타임에 볼 수 없음).

이 컴파일 문제를 해결하기 위해 당시 도입한 임시 우회가 `com.querydsl.core.types.dsl.PathBuilder<Object>`로 **JPA 엔티티 클래스명을 문자열로 참조**해 필요한 컬럼만 `NumberPath`/`StringPath`/`EnumPath`로 노출하는 방식이었다(`member`/`follow`/`rank`/`review`/`shop` 전환 시 반복 적용된 패턴, 루트 `CLAUDE.md`의 "도메인 모델 / JPA 엔티티 분리 규칙" 절에 상세 기록되어 있음).

**그런데 지금은 전 도메인(22개) 전환이 완료되어 `core-module`이 100% JPA-free 상태다.** 즉 남아 있는 PathBuilder 문자열 우회들은 전부 **이제는 같은 `infrastructure-module` 안에서 다른 도메인의 JPA 엔티티를 참조하는 infra→infra 참조**가 되었으므로, 더 이상 문자열 우회가 필요 없다. CLAUDE.md에도 "order 전환 시 정식 Q타입 조인으로 복원한 최초 사례"가 기록되어 있어, 이 프로젝트는 전환 완료 시점에 우회를 정식 조인으로 되돌리는 것을 이미 검증된 패턴으로 취급한다.

## 현재 상태 (근거 — 정확한 위치)

PathBuilder 문자열 우회가 남아 있는 곳은 다음 5곳, 4개 파일이다:

1. `infrastructure-module/src/main/java/com/tastyhouse/infrastructure/shop/persistence/ShopRepositoryImpl.java:55` — `"ReviewJpaEntity"` 문자열 참조
2. `infrastructure-module/src/main/java/com/tastyhouse/infrastructure/member/follow/persistence/MemberFollowRepositoryImpl.java:42` — `"MemberJpaEntity"` 문자열 참조
3. `infrastructure-module/src/main/java/com/tastyhouse/infrastructure/review/persistence/ReviewRepositoryImpl.java:69` — `"MemberJpaEntity"` 문자열 참조
4. `infrastructure-module/src/main/java/com/tastyhouse/infrastructure/review/persistence/ReviewRepositoryImpl.java:74` — `"ShopJpaEntity"` 문자열 참조
5. `infrastructure-module/src/main/java/com/tastyhouse/infrastructure/rank/persistence/MemberReviewRankRepositoryImpl.java:39` — `"MemberJpaEntity"` 문자열 참조

이미 존재하는 복원 참조 구현: `infrastructure-module`의 `order`/`payment`/`product` 도메인 리포지토리들이 각각 `shop`/`review`가 먼저 전환된 뒤 `QShopJpaEntity`/`QProductJpaEntity` 등 정식 Q타입 import로 되돌린 사례가 있다(CLAUDE.md "도메인 모델 / JPA 엔티티 분리 규칙" 절의 `order`, `product` 항목 참고). 이번 작업은 그 패턴을 나머지 5곳에 동일하게 적용하는 것이다.

## 작업 지시

각 파일에 대해 다음을 수행한다:

1. **`ShopRepositoryImpl.java:55`**: `"ReviewJpaEntity"` PathBuilder를 제거하고, `com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity`를 정식 import하여 조인에 사용.
2. **`MemberFollowRepositoryImpl.java:42`**: `"MemberJpaEntity"` PathBuilder를 제거하고, `com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity`를 정식 import.
3. **`ReviewRepositoryImpl.java:69, 74`**: 두 PathBuilder(`"MemberJpaEntity"`, `"ShopJpaEntity"`)를 각각 `QMemberJpaEntity`, `QShopJpaEntity` 정식 import로 교체.
4. **`MemberReviewRankRepositoryImpl.java:39`**: `"MemberJpaEntity"` PathBuilder를 `QMemberJpaEntity` 정식 import로 교체.

각 파일에서 PathBuilder로 노출했던 개별 컬럼(`NumberPath`/`StringPath`/`EnumPath`)을 정식 Q타입의 동일 필드 접근으로 1:1 치환한다. 조인 조건(`.on(...)`)과 select 대상 필드가 기존과 동일한 컬럼을 가리키는지 반드시 확인한다.

## 완료 기준

- [ ] 위 5곳 전부에서 `PathBuilder<Object>` import 및 사용이 제거됨
- [ ] `infrastructure-module` 내에서 `PathBuilder` 검색 시 (도메인 무관하게 이런 우회가 남아있는지) 0건이 되는지 grep으로 재확인
- [ ] 각 리포지토리의 QueryDSL 쿼리 결과가 기존과 동일한 컬럼·조인 의미를 가지는지 코드 리뷰로 확인 (컬럼명 오타·타입 불일치 없는지)
- [ ] 루트 `CLAUDE.md`의 해당 도메인 항목(`shop`, `member/follow`, `review`, `rank`)에 "PathBuilder 우회를 정식 Q타입으로 복원함" 한 줄 추가

## 주의사항

- 이 작업은 **순수 리팩터링**이며 동작 변경이 없어야 한다. 쿼리 결과가 바뀌면 안 됨.
- `PathBuilder` 제거 시 QueryDSL이 생성한 `QXxxJpaEntity` 클래스가 이미 `infrastructure-module`의 빌드 산출물(`build/generated/...`)에 존재하는지 확인 필요(annotation processing 대상이므로 최초 컴파일 시 생성됨).
- gradle build 테스트는 진행하지 않는다(CLAUDE.md 규칙) — 코드 검토로 대체.
- NO_COMMIT_OR_ROLLBACK — 추천 커밋 메시지: `refactor(infrastructure): PathBuilder 문자열 우회를 정식 Q타입 조인으로 복원`. 본문에 "전 도메인 JPA 분리 완료로 infra→infra 참조가 가능해져 우회가 더 이상 필요하지 않음"을 명시.

# ADR-0001: domain 모듈을 프레임워크-프리로 만들고 공유 VO에서도 JPA 어노테이션을 걷어낸다

- 상태: 채택
- 결정 커밋: `4f82b1ea7` (2026-07-21)
- 대체한 결정: —

## 맥락

도메인 모델(비즈니스 규칙을 담은 순수 객체)을 담는 모듈은 당시 `core-module`이었다. 21개 도메인의 JPA 어댑터를 `infrastructure-module`로 옮긴 뒤에도 `core-module`에는 JPA 잔재가 남아 있었다 — 컨버터 5개, `BaseEntity`, `QueryDslConfig`, 공유 `@Embeddable` VO 3개(`PhoneNumber`·`ProductDiscountInfo`·`VerificationCode`), `EntityManager`를 직접 쓰는 서비스 3개, `DatabaseConfig`.

- **VO(Value Object)**: 식별자 없이 값으로만 비교되는 작은 객체. 전화번호·할인 정보 같은 것.
- **`@Embeddable`/`@Embedded`**: JPA가 VO를 엔티티 테이블의 컬럼 묶음으로 매핑하게 하는 어노테이션.

도메인 모듈이 JPA를 알면 도메인 규칙을 바꿀 때도 영속 기술의 제약을 함께 신경 써야 하고, 도메인 타입을 쓰는 모든 모듈로 JPA 의존이 전이된다.

## 이전 규칙

### `backend/CLAUDE.md` — 도메인 모델 / JPA 엔티티 분리 규칙

- "공유 VO는 core에 어노테이션을 유지한다" — 여러 애그리거트가 함께 쓰는 `@Embedded` VO는 `core-module`에 두되 `@Embeddable`·`@Column` 어노테이션을 그대로 단다.
- 4개 앱(`WebApiApplication`/`AdminApiApplication`/`CeoApiApplication`/`BatchApplication`)의 `scanBasePackages`에 `"com.tastyhouse.core"` 항목이 있었다.

## 결정

domain 모듈은 프레임워크를 전혀 모른다. 공유 VO도 예외가 아니다.

| 항목 | before | after |
|---|---|---|
| 공유 `@Embedded` VO | `core-module`에 `@Embeddable`/`@Column` 유지 | domain에서 순수 POJO(어노테이션 없음). 컬럼 매핑은 그 VO를 쓰는 각 JpaEntity가 `@Embedded` + `@AttributeOverride`(복수 필드는 `@AttributeOverrides`)로 재선언 |
| 컨버터·`BaseEntity`·`QueryDslConfig` | `core-module` | `infrastructure-module`(지금은 `infrastructure:jpa`) |
| `EntityManager.flush()/clear()` 직접 호출 | 도메인 서비스 3곳 | Repository 계약(`saveAndFlush` 신설, 벌크 delete를 구현으로 흡수) 뒤로 은닉 |
| `DatabaseConfig` | `core-module` | 폐지. `@EnableJpaAuditing`/`@EnableTransactionManagement`는 infrastructure 설정으로 병합 |
| 모듈 의존 | data-jpa·mysql·querydsl-jpa | 제거(이 커밋에서는 querydsl-core로 교체, 이어 `17239fdf0`에서 querydsl·spring-tx·spring-orm도 제거해 production 의존이 Lombok 단독) |
| `scanBasePackages`의 domain 엔트리 | `"com.tastyhouse.core"` 포함 | 삭제(`17239fdf0`) — domain에 `@Component`/`@Service`/`@Configuration`이 0건이라 스캔할 대상이 없다 |

## 근거

- 도메인이 프레임워크를 모른다는 것을 규칙이 아니라 **빌드 그래프로 강제**하기 위해서다. domain의 production 의존이 Lombok 하나뿐이면 JPA·Spring import가 컴파일 에러가 된다.
- domain을 쓰는 api 모듈로 `com.querydsl.*`이 전이되는 경로도 원천 차단된다(`17239fdf0` 커밋 메시지).
- 공유 VO만 예외로 두면 "공유 여부"를 매번 판단해야 하고, 그 예외가 domain에 JPA 의존을 남기는 유일한 통로가 된다.

## 결과·감수한 손실

- DB 스키마·API 동작은 바뀌지 않았다(커밋 메시지 "DB 스키마·API 동작 무변경").
- 컬럼 매핑이 VO 한 곳이 아니라 VO를 쓰는 JpaEntity마다 `@AttributeOverride`로 반복된다.
- 이후 `@Embedded` 대상은 domain VO가 아니라 persistence가 소유하는 `XxxEmbeddable` record로 바뀌었다([ADR-0028](0028-persistence-state-layer.md)). persistence가 다시 domain을 쓰게 된 뒤에도 `XxxEmbeddable` 5종은 유지된다([ADR-0031](0031-persistence-domain-reallowed.md)).
- 도메인 서비스 빈 등록 위치는 이후 여러 번 바뀌었다(infrastructure `DomainServiceConfig` → `<Ctx>DomainConfig` → application → `@Service` 스캔). 그 경위는 [ADR-0027](0027-domain-ports-to-application.md)·[ADR-0034](0034-service-config-removal.md)·[ADR-0036](0036-app-marker-removal.md).

## 영향받은 문서

- `backend/CLAUDE.md` · "도메인 모델 / JPA 엔티티 분리 규칙 (선별 적용, persistence는 `infrastructure-module`로)" · 공유/비공유 VO 모두 domain에서 순수 POJO, 매핑은 JpaEntity의 `@AttributeOverride`; domain 스캔 엔트리를 두지 않는다
- `backend/AGENTS.md` · "Module Dependency Graph" · `domain`은 프레임워크를 모르고 production 의존이 0개
- `backend/domain/AGENTS.md` · "코드 주석에서 이관된 설계 근거" > "모듈 전역 — 프레임워크-프리와 명시적 저장" · 프레임워크-프리 원칙

# infrastructure-module

core-module의 순수 도메인 모델을 영속화하는 **persistence 어댑터 모듈**. 헥사고날 아키텍처에서 `core-module`이 선언한 Repository 포트(`domain/repository/XxxRepository` 인터페이스)를 JPA/QueryDSL로 구현한다. `external-api`가 파일/OAuth 어댑터를 담당하는 것과 같은 원리로, DB 어댑터를 core 밖으로 분리해 "core는 JPA 구현을 모른다"를 모듈 경계로 강제한다.

## 패키지 구조

```
com.tastyhouse.infrastructure.<도메인>.persistence/
├── XxxJpaEntity.java       @Entity — DB 매핑 전용(비즈니스 행위 없음), core.shared.entity.BaseEntity 상속
├── XxxMapper.java          도메인 ↔ 엔티티 변환 (package-private, toDomain/toEntity/applyChanges)
├── XxxJpaRepository.java   Spring Data JpaRepository<XxxJpaEntity, Long>
└── XxxRepositoryImpl.java  @Repository — core의 XxxRepository 구현 (QueryDSL 위임)
```

## 규칙

- **패키지 루트는 `com.tastyhouse.infrastructure`** — web-api/admin-api의 `scanBasePackages`에 이 패키지가 등록되어 있어야 빈이 인식된다. JPA 스캔(`@EnableJpaRepositories`/`@EntityScan`)은 이 모듈 패키지 루트의 `InfrastructurePersistenceConfig`가 `basePackageClasses`(타입 세이프)로 스스로 선언한다 — core의 `DatabaseConfig`에 이 패키지를 문자열로 넣지 않는다(core는 이 모듈을 의존하지 않아 컴파일 타임에 패키지를 볼 수 없음).
- **웹/관리 API는 이 모듈을 `runtimeOnly`로만 의존**한다 — 컴파일 타임에 어댑터 구현을 보지 못하게 해 은닉한다.
- **JPA 엔티티(`XxxJpaEntity`)는 영속 전용**: 행위 메서드를 두지 않고, 신규 생성용 정적 팩토리 `create(...)`와 update 복사용 `applyChanges(...)`만 둔다. 감사 필드는 `BaseEntity`(`@MappedSuperclass`)에서 상속.
- **저장 시맨틱은 load-copy-save**: `save(domain)`에서 id null이면 insert, id 있으면 managed 엔티티 조회 후 `applyChanges` 복사. detached merge 금지(감사 필드 파손 방지).
- **Q타입 생성 위치 주의**: `QXxxJpaEntity`는 이 모듈에서 생성(엔티티가 여기 있으므로). `@QueryProjection` result DTO의 `QXxxResult`는 여전히 core-module에서 생성된다(DTO가 core `application/dto`에 있으므로). 양쪽 build.gradle 모두 QueryDSL apt를 유지한다.
- **엔티티 enum 매핑**은 core AGENTS.md와 동일: `@Enumerated(EnumType.STRING)` + `@Column(columnDefinition = "VARCHAR(n)")`, `ORDINAL` 금지.

reference 구현: `notice` 도메인 (`NoticeJpaEntity`/`NoticeMapper`/`NoticeJpaRepository`/`NoticeRepositoryImpl`).

## Dependencies

### Internal
- `core-module` (api) — 도메인 모델·Repository 인터페이스·result DTO·`BaseEntity` 참조

### External
- `spring-boot-starter-data-jpa` (api), `mysql-connector-j`
- QueryDSL `io.github.openfeign.querydsl:querydsl-jpa:6.11` (api, OpenFeign 포크 — CVE-2024-49203 대응. 패키지명 `com.querydsl.*` 유지, 6.x부터 jpa는 `:jakarta` classifier 없이 jakarta 기본·apt만 `:jakarta` 유지)
- Lombok

<!-- MANUAL: -->

# 도메인 모델 / JPA 엔티티 분리 — 도메인별 전환 가이드

> 이 문서는 **한 도메인씩** 순수 도메인 모델과 JPA 엔티티를 분리하고, JPA 어댑터를 `infrastructure-module`로 옮기는 작업을 다른 AI/개발자가 따라 할 수 있도록 정리한 **실행 매뉴얼**이다.
> **레퍼런스 구현: `notice` 도메인** (이미 완료됨). 막히면 항상 notice의 실제 파일을 열어 그대로 대응시킨다.
> 배경/의사결정은 `md/CLEAN-ARCHITECTURE.md` 11절, 규칙 원문은 루트 `CLAUDE.md`의 "도메인 모델 / JPA 엔티티 분리 규칙" 참고. (경로는 모두 리포 루트 `tastyhouse-api/` 기준)

---

## 0. 이 작업이 무엇인가 (한 문장)

`@Entity`이면서 비즈니스 로직을 가진 도메인 모델을 → **순수 POJO 도메인 모델(core-module)** + **JPA 엔티티(infrastructure-module)** + **매퍼**로 쪼개고, persistence 어댑터를 `infrastructure-module`로 이동한다. **DB 스키마·API 동작·파사드는 바뀌지 않는다.**

## 대상 도메인을 고르는 기준

- **전환 우선**: 상태전이·불변식이 코드에 실재하는 도메인 (order, payment, coupon, point, reservation 등 — 엔티티에 `update()`, `cancel()`, `complete()` 같은 행위 메서드가 있음).
- **전환 안 해도 됨**: 단순 CRUD 도메인은 현행(도메인 모델 = `@Entity`, persistence가 core-module 내부) 유지 허용. **분리는 강제가 아니다.**
- 한 번에 **한 도메인만** 한다. PR도 도메인 단위로 분리한다.

---

## 1. 전환 전 파악할 것 (체크리스트)

작업할 도메인을 `<Xxx>`(예: `Order`), 소문자를 `<xxx>`(예: `order`)라 하자. 시작 전에 아래를 읽어 현재 구조를 파악한다.

- [ ] `core-module/.../domain/<xxx>/domain/model/<Xxx>.java` — 엔티티의 필드, `@Column` 매핑, 행위 메서드(`of`/`update`/…), VO getter(`get<Xxx>Id()`)
- [ ] `core-module/.../domain/<xxx>/domain/repository/<Xxx>Repository.java` — 인터페이스 시그니처 (이건 **바뀌지 않는다**)
- [ ] `core-module/.../domain/<xxx>/infrastructure/persistence/` — `<Xxx>JpaRepository`, `<Xxx>RepositoryImpl` (이 둘을 infrastructure-module로 옮긴다)
- [ ] `core-module/.../domain/<xxx>/application/<Xxx>CommandService.java` — **더티 체킹에 의존하는 곳 찾기**: `findById(...)` 후 엔티티를 `update()`/상태변경만 하고 `save()`를 안 부르는 메서드 (여기에 명시적 `save`를 넣어야 함)
- [ ] `core-module/.../domain/<xxx>/application/<Xxx>QueryService.java` — 조회에서 엔티티의 어떤 getter를 쓰는지 (도메인 모델이 그 getter를 유지해야 함. 특히 `getCreatedAt()`/`getUpdatedAt()`)
- [ ] `application/dto/result/*` — `@QueryProjection` result DTO는 **그대로 둔다**(core-module에 남음)

> **연관관계 매핑(`@OneToMany`/`@ManyToOne` 등)이 있는 도메인은 주의**: notice는 연관관계가 없어 단순했다. 자식 엔티티/컬렉션이 있으면 매퍼가 그 조립까지 책임져야 하므로 난이도가 올라간다. 이런 도메인은 별도로 신중히 진행한다.

---

## 2. 단계별 작업

### Step 1. 순수 도메인 모델로 재작성 (`core-module`, 위치 그대로)

`domain/model/<Xxx>.java`에서 JPA를 걷어내고 POJO로 만든다.

- **제거**: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `extends BaseEntity`, `@NoArgsConstructor`, 모든 `jakarta.persistence.*` import.
- **유지**: lombok `@Getter`(JPA 아님 — 유지 가능), 행위 메서드(`update`/`delete`/상태전이), `get<Xxx>Id()`.
- **필드**: `id`는 `private final Long id;`(미영속이면 null). 감사 시각은 QueryService가 쓰는 것만 필드로 둔다 — 보통 `private final LocalDateTime createdAt;` (+ 조회 응답이 `updatedAt`도 쓰면 `updatedAt`도).
- **팩토리 2개만 공개**:
  - `public static <Xxx> of(...)` — **신규 생성**(id·감사시각 없음, 기존 `of`와 동일 파라미터).
  - `public static <Xxx> reconstitute(Long id, ..., LocalDateTime createdAt, LocalDateTime updatedAt)` — **DB 재구성 전용**. 인프라(매퍼)만 호출. 불변식 우회 방지를 위해 Javadoc에 "영속 계층 전용" 명시.
- private 생성자는 전체 필드를 받는 형태 하나로 두고 두 팩토리가 이를 호출.

**notice 레퍼런스**: `core-module/src/main/java/com/tastyhouse/core/domain/notice/domain/model/Notice.java` — 이 파일을 열고 필드/팩토리 골격을 그대로 본뜬다.

```java
// 핵심 골격 (notice 기준)
@Getter
public class Notice {
    private final Long id;                 // null이면 미영속
    private String title;
    private boolean deleted;
    private final LocalDateTime createdAt; // 재구성 시에만 값
    private final LocalDateTime updatedAt;

    private Notice(Long id, ..., LocalDateTime createdAt, LocalDateTime updatedAt) { ... }

    public static Notice of(String title, ...) {              // 신규
        return new Notice(null, ..., false, null, null);
    }
    public static Notice reconstitute(Long id, ..., LocalDateTime createdAt, LocalDateTime updatedAt) { // 재구성(인프라 전용)
        return new Notice(id, ..., createdAt, updatedAt);
    }
    public NoticeId getNoticeId() { return NoticeId.of(this.id); }
    public void update(...) { ... }
    public void delete() { this.deleted = true; }
}
```

### Step 2. `<Xxx>JpaEntity` 신설 (`infrastructure-module`)

경로: `infrastructure-module/src/main/java/com/tastyhouse/infrastructure/<xxx>/persistence/<Xxx>JpaEntity.java`
패키지: `com.tastyhouse.infrastructure.<xxx>.persistence`

- **Step 1에서 걷어낸 JPA 매핑을 여기에 그대로 옮긴다**: `@Entity`, `@Table(name = "<TABLE>")`, `extends BaseEntity`, `@Id @GeneratedValue(IDENTITY) Long id`, 모든 `@Column`, `@NoArgsConstructor(access = PROTECTED)`, `@Getter`. **테이블/컬럼명·타입은 원본과 100% 동일**(DDL·`ddl-auto=validate` 무변경).
- **행위 메서드는 두지 않는다.** 대신 매퍼가 쓸 두 메서드만 `package-private`으로:
  - `static <Xxx>JpaEntity create(...)` — 신규 저장용(식별자 없음).
  - `void applyChanges(...)` — managed 엔티티에 변경 필드 복사(update용).
- **enum 필드가 있으면** `@Enumerated(EnumType.STRING)` + `@Column(columnDefinition = "VARCHAR(n)")` 규칙 그대로(루트 CLAUDE.md "enum ↔ DB 컬럼 매핑 규칙").

**notice 레퍼런스**: `infrastructure-module/src/main/java/com/tastyhouse/infrastructure/notice/persistence/NoticeJpaEntity.java`

### Step 3. `<Xxx>Mapper` 신설 (`infrastructure-module`, package-private)

경로: 같은 `persistence` 폴더. `final class <Xxx>Mapper`(private 생성자, 정적 메서드 3개):

- `static <Xxx> toDomain(<Xxx>JpaEntity entity)` — `<Xxx>.reconstitute(entity.getId(), ..., entity.getCreatedAt(), entity.getUpdatedAt())` 호출.
- `static <Xxx>JpaEntity toEntity(<Xxx> domain)` — `<Xxx>JpaEntity.create(...)`(신규).
- `static void applyChanges(<Xxx>JpaEntity entity, <Xxx> domain)` — `entity.applyChanges(domain.getXxx(), ...)`.

> **주의**: 파라미터가 같은 타입(String/Long 등) 여러 개면 **순서 착오로 값이 뒤바뀌는 조용한 버그**가 난다. 필드 선언 순서 · `reconstitute` 파라미터 순서 · 매퍼가 넘기는 인자 순서를 하나씩 대조한다.

**notice 레퍼런스**: `infrastructure-module/src/main/java/com/tastyhouse/infrastructure/notice/persistence/NoticeMapper.java`

### Step 4. `<Xxx>JpaRepository` 이동 (`infrastructure-module`)

- 기존 `core-module`의 `<Xxx>JpaRepository`를 `infrastructure-module`의 persistence 폴더로 옮기고, 제네릭 타입을 `JpaRepository<<Xxx>JpaEntity, Long>`으로 바꾼다(도메인 모델이 아니라 JpaEntity).

**notice 레퍼런스**: `infrastructure-module/src/main/java/com/tastyhouse/infrastructure/notice/persistence/NoticeJpaRepository.java`

### Step 5. `<Xxx>RepositoryImpl` 이동 + save 재구현 (`infrastructure-module`)

기존 `RepositoryImpl`을 옮기고 아래를 반영한다.

- **QueryDSL Q타입 치환**: `Q<Xxx>`(도메인 모델용, 이제 없음) → **`Q<Xxx>JpaEntity`**(infra에서 생성됨). static import도 `import static com.tastyhouse.infrastructure.<xxx>.persistence.Q<Xxx>JpaEntity.<xxx>JpaEntity;`로 변경.
- **result DTO projection은 그대로**: `Q<Xxx>ListItemResult` 등은 여전히 core-module에서 생성되므로 import 경로·인자 순서 변경 없음.
- **`findById`**: 조회 후 `.map(<Xxx>Mapper::toDomain)`으로 도메인 반환.
- **`save(<Xxx> domain)` — load-copy-save 패턴 (⚠️ 핵심)**:

```java
@Override
public Notice save(Notice notice) {
    if (notice.getId() == null) {                          // 신규 → insert
        NoticeJpaEntity saved = noticeJpaRepository.save(NoticeMapper.toEntity(notice));
        return NoticeMapper.toDomain(saved);
    }
    // 기존 → managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트, 추가 쿼리 없음) 후 변경 필드만 복사
    NoticeJpaEntity entity = noticeJpaRepository.findById(notice.getId())
        .orElseThrow(() -> new IllegalStateException("존재하지 않는 ...: " + notice.getId()));
    NoticeMapper.applyChanges(entity, notice);
    return NoticeMapper.toDomain(entity);
}
```

> **detached merge(`save(엔티티)`로 통째 저장) 금지** — `@CreatedDate(updatable=false)` 감사 필드가 깨지고 전 필드 UPDATE가 발생한다. 반드시 managed 엔티티 조회 후 `applyChanges`.
> **soft delete 주의**: `findById`에 `deleted.isFalse()` 필터가 있으면, update 경로의 managed 조회는 그 필터 없는 순수 PK 조회(`jpaRepository.findById`)를 써야 삭제 플래그 갱신이 동작한다. notice가 이 형태다.

**notice 레퍼런스**: `infrastructure-module/src/main/java/com/tastyhouse/infrastructure/notice/persistence/NoticeRepositoryImpl.java`

### Step 6. core-module의 기존 persistence 삭제

- `core-module/.../domain/<xxx>/infrastructure/persistence/`의 `<Xxx>JpaRepository.java`·`<Xxx>RepositoryImpl.java` 삭제. 폴더가 비면 `infrastructure/persistence`·`infrastructure` 빈 디렉토리도 제거.
- **삭제 후 잔여 참조 확인**: `grep -rn "domain.<xxx>.infrastructure\|Q<Xxx>\b" --include="*.java" .`(단, `Q<Xxx>ListItemResult` 등 result Q타입은 정상이므로 제외). 0건이어야 한다.

### Step 7. `<Xxx>CommandService`에 명시적 save 추가 (`core-module`)

Step 1 체크리스트에서 찾은 **"변경 후 save를 안 부르던" 모든 command 메서드**에 `<xxx>Repository.save(<xxx>)`를 추가한다. 더티 체킹이 사라졌으므로 이게 없으면 **변경이 조용히 유실된다.**

```java
public void updateNotice(NoticeId noticeId, NoticeUpdateCommand command) {
    Notice notice = noticeRepository.findById(noticeId).orElseThrow(...);
    notice.update(command.title(), command.content(), command.visible());
    noticeRepository.save(notice);   // ← 추가
}
public void deleteNotice(NoticeId noticeId) {
    Notice notice = noticeRepository.findById(noticeId).orElseThrow(...);
    notice.delete();
    noticeRepository.save(notice);   // ← 추가
}
```

**notice 레퍼런스**: `core-module/src/main/java/com/tastyhouse/core/domain/notice/application/NoticeCommandService.java`

### Step 8. 순수 단위 테스트 추가 (`core-module`)

경로: `core-module/src/test/java/com/tastyhouse/core/domain/<xxx>/domain/model/<Xxx>Test.java`
Spring/JPA 없이 `of` → 상태전이(`update`/`delete`/…) → `reconstitute` 불변식을 plain JUnit5 + AssertJ로 검증(분리로 얻는 테스트 용이성의 증명이자 회귀 방지).

**notice 레퍼런스**: `core-module/src/test/java/com/tastyhouse/core/domain/notice/domain/model/NoticeTest.java`

---

## 3. 조립(Spring/Gradle) — 첫 도메인에서 이미 다 됨, 신규 도메인은 확인만

아래는 **infrastructure-module 자체를 만들 때(=notice) 이미 완료**되었다. 새 도메인 전환 시엔 **손댈 필요 없고, 그대로 있는지 확인만** 한다.

- [x] `settings.gradle` — `include 'infrastructure-module'`
- [x] `infrastructure-module/build.gradle` — QueryDSL apt 설정 등 (core-module과 동일)
- [x] `web-api/build.gradle`·`admin-api/build.gradle` — `runtimeOnly project(':infrastructure-module')`
- [x] `WebApiApplication`·`AdminApiApplication` — `scanBasePackages`에 `com.tastyhouse.infrastructure`
- [x] `infrastructure-module`의 `InfrastructurePersistenceConfig`(패키지 루트) — 이 모듈의 `@EnableJpaRepositories`/`@EntityScan`을 `basePackageClasses`로 자체 선언. **(갱신)** 전 도메인 이동 완료 후 core의 `DatabaseConfig`는 폐지되었고, `@EnableJpaAuditing`/`@EnableTransactionManagement` 전역 설정도 이 클래스로 병합되었다(7절 참고).

> 즉 **두 번째 도메인부터는 Step 1~8만** 하면 된다.

---

## 4. 완료 검증 (DoD)

- [ ] `<Xxx>Repository` **인터페이스 시그니처 무변경** (구현 위치만 이동).
- [ ] **파사드(web-api/admin-api의 `<Xxx>Service`) 무변경** — application 서비스만 호출하므로. `grep -rn "import com.tastyhouse.core.domain.<xxx>" web-api/src admin-api/src`로 application/vo만 참조하는지 확인.
- [ ] 삭제된 core persistence·`Q<Xxx>`(도메인 모델 Q타입) **잔여 참조 0건**.
- [ ] result DTO projection **인자 순서**와 `Q<Xxx>ListItemResult` 필드 순서 일치.
- [ ] 매퍼 파라미터 순서 대조 완료(같은 타입 다수 파라미터 뒤바뀜 없음).
- [ ] command 서비스의 모든 변경 메서드에 `save` 호출 존재.
- [ ] **컴파일**: IDE Gradle 리로드 후 `./gradlew :web-api:compileJava :admin-api:compileJava` 성공.
- [ ] **부팅**: 앱 기동 시 `ddl-auto=validate` 통과(= JPA 매핑이 DB 스키마와 동일함을 증명). notice/해당 도메인 CRUD API 동작 동일.
- [ ] **단위 테스트**: `<Xxx>Test` 통과.

> 프로젝트 규칙상 **커밋은 직접 하지 않고**(NO_COMMIT_OR_ROLLBACK), 추천 커밋 메시지만 제시한다. gradle build 자동 실행도 하지 않는다(사람이 확인).

---

## 5. 문서 갱신 (전환한 도메인마다)

이 패턴 문서·규칙은 이미 있으므로 **매번 새로 쓰지 않는다.** 대신 전환한 도메인을 아래에 reference로 **한 줄 추가**만 한다.

- 루트 `CLAUDE.md` "도메인 모델 / JPA 엔티티 분리 규칙"의 reference 목록에 `<xxx>` 추가.
- `md/CLEAN-ARCHITECTURE.md` 11절의 "전환 완료 도메인" 목록에 `<xxx>` 추가.
- 새 규칙(예: 연관관계 매핑 조립 방식)이 생겼다면 그때만 규칙 본문을 보강한다.

---

## 6. 자주 하는 실수 (notice에서 겪은 것 포함)

| 실수 | 증상 | 예방 |
|---|---|---|
| command 메서드에 `save` 누락 | update/delete가 DB에 반영 안 됨(조용히) | Step 1 체크리스트에서 "save 없이 변경만 하던 메서드"를 미리 다 찾아둔다 |
| `reconstitute`에 `updatedAt` 누락 | QueryService가 `getUpdatedAt()` 쓰면 컴파일/응답 오류 | Step 0에서 QueryService가 쓰는 getter를 먼저 확인. 조회 응답이 `updatedAt`을 쓰면 도메인·`reconstitute`·매퍼에 모두 포함 |
| detached merge 사용 | 감사 필드 파손, 전 필드 UPDATE | load-copy-save만 사용(managed 조회 후 `applyChanges`) |
| soft delete 도메인에서 update 경로가 필터된 조회 사용 | 삭제 플래그 갱신 안 됨 | update 경로 managed 조회는 `jpaRepository.findById`(필터 없는 PK 조회) |
| 매퍼 인자 순서 뒤바뀜 | 컴파일은 되나 값이 섞임 | 필드/`reconstitute`/호출 인자 순서 3중 대조 |
| Q타입 혼동 | 엔티티 Q타입을 core에서 찾음 | `Q<Xxx>JpaEntity`=infra, `Q<Xxx>Result`=core |
| `@Column` 매핑 누락/변경 | 부팅 시 `ddl-auto=validate` 실패 | JpaEntity의 테이블/컬럼을 원본과 100% 동일하게 |

---

## 7. 전 도메인 이동 완료 후 — core-module 100% JPA-free 마무리 (완료)

21개 전 도메인의 도메인별 전환(1~6절)이 끝난 뒤에도, core-module에는 도메인 persistence 외의 **JPA 공용 잔재**가 남아 있었다. 이를 정리해 core-module을 완전한 JPA-free 모듈로 만드는 마무리 작업을 별도로 진행했다(순서 의존이 있어 도메인별 전환과 달리 한 번에 진행).

- **converter 5개**(`OrderIdConverter`/`PaymentIdConverter`/`AmountConverter`/`MemberIdConverter` + 미사용 죽은 코드였던 `PaymentRefundIdConverter`) — core의 `domain/<xxx>/infrastructure/persistence/converter/`에서 infrastructure-module의 대응 도메인 `persistence/` 패키지로 이동(죽은 코드는 삭제). `@Convert`를 쓰는 JpaEntity들의 import만 갱신.
- **`BaseEntity`** — `core/shared/entity/BaseEntity.java` → `infrastructure/shared/persistence/BaseEntity.java`. `extends BaseEntity`하는 JpaEntity 59개의 import를 일괄 갱신.
- **`QueryDslConfig`**(`JPAQueryFactory` 빈) — `core/config/` → `infrastructure/config/`로 이동. Spring DI 주입이라 사용처(RepositoryImpl 47개) 수정 불필요.
- **공유 `@Embeddable` VO 3개**(`PhoneNumber`/`ProductDiscountInfo`/`VerificationCode`) — `@Embeddable`/`@Column`/`jakarta.persistence` import를 완전히 제거해 순수 POJO화. 대신 이를 `@Embedded`로 쓰던 JpaEntity 4곳(`MemberJpaEntity`/`EventWinnerJpaEntity`/`PhoneVerificationJpaEntity`/`ProductJpaEntity`)에 `@AttributeOverride`(`ProductJpaEntity`는 `@AttributeOverrides`)를 추가해 컬럼 매핑을 재선언. **이 단계는 VO 수정과 JpaEntity의 override 추가를 반드시 같은 커밋으로 묶어야** `ddl-auto=validate`가 깨지지 않는다(override 없이 `@Column` 제거만 하면 기본 컬럼명이 필드명 `value`로 바뀌어 검증 실패).
- **`EntityManager` 직접 사용 서비스 3개** — `ReservationCreator`/`SearchKeywordCommandService`/`RankCommandService`가 `entityManager.flush()`/`.clear()`를 직접 호출하던 것을 Repository 계약 뒤로 은닉:
  - 벌크 delete(`SearchKeywordLogRepository`/`MemberReviewRankRepository`) 뒤 flush+clear가 필요한 두 곳은 impl(`PopularKeywordRepositoryImpl#deleteAll`, `MemberReviewRankRepositoryImpl#deleteByRankTypeAndBaseDate`) 말미로 흡수(인터페이스 시그니처 불변).
  - 낙관적 락/유니크 충돌을 커밋 전에 즉시 노출해야 하는 `ReservationCreator`는 `ReservationSlotRepository`에 `saveAndFlush(ReservationSlot)`을 신설해 `save`+`flush`를 원자적으로 수행.
- **`DatabaseConfig` 폐지** — `core/config/DatabaseConfig.java`(`@EnableJpaRepositories`/`@EntityScan`/`@EnableJpaAuditing`/`@EnableTransactionManagement`)를 삭제하고, `@EnableJpaAuditing`/`@EnableTransactionManagement`를 infrastructure-module의 `InfrastructurePersistenceConfig`로 병합(JPA 스캔 설정과 전역 설정을 한 곳에 응집).
- **`core-module/build.gradle` 정리** — `spring-boot-starter-data-jpa`·`mysql-connector-j` 제거, `querydsl-jpa` → `querydsl-core`로 교체(core는 `@QueryProjection` result DTO만 생성하므로 `com.querydsl.core.*`만 있으면 충분 — 유일한 `com.querydsl.jpa` 사용처였던 `QueryDslConfig`가 infra로 이동해 성립), `jakarta.persistence-api` annotationProcessor 제거. `@Transactional`/`@Service` 등은 루트 `build.gradle`의 `subprojects { implementation 'spring-boot-starter' }` 전이 의존으로 계속 커버되어 별도 추가 불필요.
- **JPA/DB 설정 YAML 이동** — `core/src/main/resources/application-core.yml`(datasource·hibernate `ddl-auto`·mysql driver·`spring.sql.init` 등 100% JPA/DB 설정, core resources의 유일한 파일이었음)을 `infrastructure-module/src/main/resources/application-infrastructure.yml`로 이동·리네이밍. 이 설정을 실제로 구동하는 JPA/MySQL 의존성과 `InfrastructurePersistenceConfig`(JPA 전역 설정)가 이미 infra에 있어, YAML만 core에 남아 있던 비대칭을 정합시켰다. `web-api`/`admin-api`의 `application.yml`의 `spring.config.import` 2곳을 새 파일명으로 갱신(`application-external.yml` 참조 패턴과 동일). 이동 후 `core-module/src/main/resources`는 빈 디렉토리라 함께 삭제.

완료 검증(4절 DoD에 추가):
- [x] `grep -rln "jakarta.persistence\|EntityManager\|AttributeConverter\|BaseEntity\|com.querydsl.jpa" core-module/src/main/java` → 0건.
- [x] `core-module/build.gradle`에 `data-jpa`/`mysql`/`querydsl-jpa` 없음, `querydsl-core`로 대체.
- [x] `core-module/src/main/resources` 디렉토리 없음(JPA/DB 설정 YAML도 infrastructure-module로 이동 완료).
- [ ] (사람 확인 필요) 컴파일·부팅·`ddl-auto=validate`·단위 테스트 — 특히 `PhoneNumber`/`ProductDiscountInfo`/`VerificationCode`를 쓰는 4개 JpaEntity의 컬럼(`phone_number`/`discount_price`/`discount_rate`/`verification_code`) 매핑.

이 마무리 작업으로 규칙 문서(루트 `CLAUDE.md`)의 "공유 `@Embeddable` VO는 core에 어노테이션 유지" 원칙이 "VO는 core에서 순수 POJO, 컬럼 매핑은 infra JpaEntity의 `@AttributeOverride`로"로 역전되었다. 상세는 루트 `CLAUDE.md`의 "도메인 모델 / JPA 엔티티 분리 규칙" 개정 내용과 `md/CLEAN-ARCHITECTURE.md` 참고.

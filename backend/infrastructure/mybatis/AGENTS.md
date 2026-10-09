<!-- Parent: ../../AGENTS.md -->

# infrastructure:mybatis

**MyBatis로 구현한 영속 어댑터를 담는 모듈(`java-library`).** 지금은 banner 쓰기 포트 `BannerLoadPort`·`BannerSavePort`의 MyBatis 구현 하나만 있다. 같은 포트를 `infrastructure:jpa`의 JPA 구현(jpa 모듈 분리 전에는 `infrastructure:persistence`)도 갖고 있으며, 속성 `persistence.banner.write.provider`로 둘 중 하나만 빈으로 등록된다(기본 `jpa`).

## 용어 풀이

- **포트**: application 모듈의 인터페이스. 서비스는 이것만 안다 — 예: `application/.../banner/port/out/write/{BannerLoadPort,BannerSavePort}`(`findById`, `save`).
- **어댑터**: 포트를 실제 기술로 구현한 클래스. 이 모듈의 `BannerMyBatisPersistenceAdapter`가 MyBatis로 구현한다.
- **JPA(ORM)**: 자바 객체(엔티티)를 테이블에 자동으로 대응시킨다. SQL을 직접 쓰지 않고, 엔티티 값을 바꾸면 트랜잭션 끝에 UPDATE가 나간다(변경 감지).
- **MyBatis(SQL 매퍼)**: SQL을 XML에 직접 쓰고, 결과 행을 자바 객체로 옮긴다. 무엇이 실행되는지 SQL 그대로 보인다.
- **1차 캐시**: JPA가 한 트랜잭션 안에서 로드한 엔티티를 기억하는 공간. MyBatis는 이것을 모르므로 같은 행을 두 기술로 섞어 쓰면 값이 어긋날 수 있다 — 이 모듈은 스위치로 한쪽만 켜서 이를 피한다.
- **`@ConditionalOnProperty`**: 설정값이 조건에 맞을 때만 그 클래스를 빈으로 등록하는 Spring Boot 애노테이션.

## 왜 이 모듈이 있나

1. **포트는 그대로 두고 구현 기술만 바꿀 수 있다는 것을 실제 코드로 보이기 위해서다.** banner 서비스 3개(`BannerCreateService`·`BannerUpdateService`·`BannerDeleteService`)는 JPA든 MyBatis든 한 줄도 바뀌지 않는다.
2. **JPA로 언제든 되돌릴 수 있게 하기 위해서다.** 코드를 지우지 않고 설정값 하나로 전환한다.
3. **MyBatis를 필요한 앱에만 싣기 위해서다.** 처음 파일럿에서는 MyBatis가 `infrastructure:persistence` 안에 있어 4앱 전부에서 MyBatis 자동 설정이 켜졌다. 모듈을 나누면서 이 모듈을 의존하는 admin-api에서만 켜진다(backend/CLAUDE.md "클래스패스 존재 = 활성화").

참고한 구조: board-project(헥사고날 멀티모듈 예제 프로젝트)의 `adapter-out-persistence-jpa`·`-mybatis` 분리. 다른 점은 board-project가 포트마다 한 기술만 구현(JPA=쓰기, MyBatis=조회)하는 반면, 여기서는 **같은 포트를 두 모듈이 모두 구현**하고 속성으로 고른다는 것이다. JPA 모듈 `infrastructure:jpa`(jpa 모듈 분리 전 `infrastructure:persistence`)에 어댑터 100여 개가 함께 있어 admin-api가 그 모듈을 뺄 수 없기 때문이다.

## 패키지 구조

```
com.tastyhouse.infrastructure.mybatis/
├── MyBatisModuleConfig.java              @MapperScan(basePackageClasses = MyBatisModuleConfig.class, annotationClass = Mapper.class)
└── banner/
    ├── BannerMyBatisPersistenceAdapter   BannerLoadPort·BannerSavePort 구현 — provider=mybatis일 때만 등록
    ├── BannerMyBatisMapper               MyBatis SQL 인터페이스(@Mapper)
    ├── BannerRow                         조회 행(record, XML <constructor>의 이름 기반 매핑)
    ├── BannerWriteRow                    쓰기 행(INSERT 생성 키를 setId로 돌려받는다)
    └── BannerRowMapper                   행 ↔ 도메인 변환
src/main/resources/
├── application-mybatis.yml               mybatis.mapper-locations: classpath*:mapper/**/*.xml
└── mapper/banner/BannerMyBatisMapper.xml selectActiveById · insert · update
```

- 루트 패키지는 `com.tastyhouse.infrastructure.mybatis`다(backend/CLAUDE.md "infrastructure 패키지 규칙").
- 지금은 쓰기만 있어 컨텍스트 아래를 `persistence`/`query`로 나누지 않는다. 조회 어댑터를 추가하면 jpa 모듈처럼 `{ctx}.{persistence|query}`로 나눈다.
- 이름: 이 저장소에서 `XxxMapper`는 도메인 변환기다. 그래서 MyBatis SQL 인터페이스는 `XxxMyBatisMapper`, 변환기는 `XxxRowMapper`로 짓는다. 같은 포트를 JPA 구현과 함께 가지므로 어댑터는 `XxxMyBatisPersistenceAdapter`다(backend/CLAUDE.md "아웃바운드 포트·어댑터 네이밍 규칙"의 예외).

## 전환 방법

| 하고 싶은 것 | 방법 |
|---|---|
| MyBatis로 바꾸기 | admin-api를 `BANNER_WRITE_PROVIDER=mybatis`로 기동(또는 `--persistence.banner.write.provider=mybatis`) |
| JPA로 되돌리기 | 환경변수를 지우거나 `jpa`로 — 기본값이 jpa다 |
| 잘못된 값(`foo`) | 구현이 0개 → admin-api가 `BannerLoadPort`·`BannerSavePort` 빈 없음으로 **기동 실패**(의도된 동작) |

MyBatis 구현은 이 모듈을 의존하는 앱에서만 쓸 수 있다. 지금은 admin-api뿐이다(`admin-api/build.gradle`의 `runtimeOnly project(':infrastructure:mybatis')` + `application.yml`의 `classpath:application-mybatis.yml`).

## JPA 구현과의 비교 (banner 쓰기)

| 항목 | JPA (`infrastructure:jpa`) | MyBatis (이 모듈) |
|---|---|---|
| 클래스 | `BannerJpaPersistenceAdapter` + `BannerJpaMapper` + `BannerJpaRepository` + `BannerJpaEntity` | `BannerMyBatisPersistenceAdapter` + `BannerRowMapper` + `BannerMyBatisMapper` + `BannerRow`/`BannerWriteRow` + XML |
| `findById` | QueryDSL `selectFrom … where id = ? and deleted = false` | XML `selectActiveById`(같은 조건) |
| 신규 저장 | `jpaRepository.save(entity)` — IDENTITY 키를 엔티티가 받음 | `insert`(`useGeneratedKeys`) — 키를 `BannerWriteRow#setId`로 받음 |
| 수정 저장 | PK로 managed 엔티티를 로드해 값 복사 → 변경 감지로 UPDATE | `UPDATE … WHERE id = ?` 한 문장, 영향 행 0이면 예외 |
| 없는 id 수정 | `IllegalStateException("존재하지 않는 배너입니다: …")` | 같은 예외·같은 메시지 |
| `created_at`/`updated_at` | `AuditingEntityListener`가 앱 시각으로 채움 | 어댑터가 `LocalDateTime.now()`로 채움 |
| 값이 안 바뀐 저장 | UPDATE 생략 → `updated_at` 그대로 | 항상 UPDATE → `updated_at` 갱신 (**유일한 의미 차이**, banner 서비스는 항상 값을 바꾼 뒤 저장해 실사용 차이 없음) |
| 실행 SQL 확인 | p6spy의 select에 `bje1_0.` 별칭(엔티티명 `BannerJpaEntity`에서 온다) | p6spy에 XML 그대로 `SELECT id, type, title, image_file_id, …` |
| 장점 | 엔티티만 고치면 SQL이 따라옴, 스키마 검증(`ddl-auto: validate`) | SQL이 눈에 보이고 복잡한 쿼리를 그대로 쓸 수 있음 |
| 단점 | 변경 감지·1차 캐시를 알아야 함 | 감사 필드·낙관적 락·소프트 삭제 조건을 직접 써야 함, 스키마 검증 없음 |

## 배선

- `MyBatisModuleConfig`(`@Configuration(proxyBeanMethods = false)`, `@ComponentScan` 없음)는 앱 `ModuleScanConfig`의 `com.tastyhouse.infrastructure` 스캔으로 등록된다. `@MapperScan`은 지우지 않는다 — MyBatis 기본 매퍼 스캔은 앱의 auto-config 패키지(`com.tastyhouse.adminapi`)만 본다.
- 트랜잭션 매니저를 따로 만들지 않는다. admin-api의 `JpaTransactionManager`가 같은 DataSource 커넥션을 바인딩하므로 MyBatis가 서비스의 `@Transactional`에 그대로 참여한다.
- `mapper-locations`는 `classpath*:`여야 한다. `classpath:`는 첫 `mapper/` 디렉터리 하나만 찾아, 앞선 jar에 같은 디렉터리가 생기면 부팅은 성공하고 첫 쿼리에서 `BindingException`이 난다.

## Dependencies

### Internal
- `domain` (implementation) — 도메인 모델 `Banner`·VO
- `application` (implementation) — 구현할 포트 `BannerLoadPort`·`BannerSavePort`(`..port.out..`만 참조)

### External
- `org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3` (implementation — Spring Boot 3.2.x 호환 라인)
- MySQL 드라이버는 이 모듈이 선언하지 않는다 — 조립 모듈 `infrastructure:persistence`가 `runtimeOnly`로 싣는다(JPA·MyBatis 공용 드라이버라 어느 한 구현 모듈이 아니라 조립 모듈이 소유한다. `../persistence/AGENTS.md`). admin-api가 persistence를 의존하므로 이 모듈과 함께 실린다.
- datasource(`spring.datasource.*`)와 provider 키 `persistence.banner.write.provider`도 같은 조립 모듈의 `application-infrastructure.yml`이 소유한다. 이 모듈의 `application-mybatis.yml`은 MyBatis 자체 설정만 갖는다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### `BannerMyBatisProviderConditionTest` — MyBatis 구현은 provider=mybatis일 때만 등록

**대상**: `backend/infrastructure/mybatis/src/test/java/com/tastyhouse/infrastructure/mybatis/banner/BannerMyBatisProviderConditionTest.java` · `backend/infrastructure/mybatis/src/main/java/com/tastyhouse/infrastructure/mybatis/banner/BannerMyBatisPersistenceAdapter.java` → 클래스의 `@ConditionalOnProperty`

원문 취지: `mybatis`면 등록, 속성 없음·`jpa`·알 수 없는 값이면 미등록. jpa 모듈의 `BannerJpaProviderConditionTest`(`backend/infrastructure/jpa/src/test/java/com/tastyhouse/infrastructure/jpa/banner/persistence/`)와 짝이다. **이 클래스에 `matchIfMissing = true`를 붙이지 않는다** — 속성이 없을 때 JPA 구현과 함께 2개가 등록돼 admin-api가 `NoUniqueBeanDefinitionException`으로 기동하지 못한다.

### `architecture/PackageRootTest` · `architecture/LayerRulesTest`

**대상**: `backend/infrastructure/mybatis/src/test/java/com/tastyhouse/infrastructure/mybatis/architecture/PackageRootTest.java` → `shouldResideInModuleRootPackage` · `topLevelClassesShouldNotBePublic`, 같은 패키지 `LayerRulesTest.java` → `shouldNotDependOnApiModules` · `shouldNotDependOnOtherPersistenceAdapters`

원문 취지: 모든 클래스는 `com.tastyhouse.infrastructure.mybatis..`에 있고 최상위 클래스는 public이 아니다. application은 `..port.out..`만 참조하고, `com.tastyhouse.infrastructure.jpa..`(jpa 모듈 분리 전 `com.tastyhouse.infrastructure.persistence..`)를 의존하지 않는다(영속 어댑터 모듈끼리는 서로 모른다). `shouldNotDependOnOtherPersistenceAdapters`는 이 모듈의 클래스패스에 jpa 모듈이 없어 지금은 위반이 컴파일조차 안 되지만, 의존이 추가되는 회귀를 막는 방어선이라 지우지 않는다.

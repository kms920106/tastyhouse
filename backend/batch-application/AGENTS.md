# batch-application

배치 잡의 **application 계층**을 소유하는 모듈. 잡 UseCase 인바운드 포트(`<job>/port/in/`), 그 구현인 `*SchedulerService`, 트랜잭션 경계를 나누는 `*Executor`/`*Runner`, 그리고 BBQ 크롤링 동기화(`crawling/bbq/`)가 여기 있다.

`@Scheduled` 트리거(driving adapter)와 부트스트랩(`BatchApplication`)은 `batch-module`에 남아 있다(`batch-module/AGENTS.md`).

## 신설 배경 (챕터 01)

`batch-module` 하나가 어댑터와 application 계층을 함께 담고 있었다. 두 계층이 같은 모듈 안 다른 패키지였을 뿐이라 경계가 **규율로만** 유지됐고, ArchUnit이 사후에 잡을 수는 있어도 "잡 로직이 트리거로 새는 것"을 컴파일 단계에서 막지는 못했다. 모듈을 나누면 그 경계가 빌드 그래프가 된다.

**이 챕터는 전체 모듈 재편 프로그램의 파일럿**이다 — 여기서 확정한 레시피(모듈 신설 → `git mv` → 패키지 리네임 → config 배선 → ArchUnit 분할)를 02~04(web/admin/ceo)가 반복한다.

## 패키지 구조

```
com.tastyhouse.batchapplication/
├── BatchApplicationConfig.java    @ComponentScan 진입점 — 쓰는 앱이 @Import 한다
├── <job>/port/in/                 잡 UseCase 인터페이스 7개 (입력이 없어 Command record 없음)
├── <job>/service/                 *SchedulerService implements {Job}UseCase + *Executor/*Runner
├── crawling/bbq/                  BBQ 크롤링 동기화 (아래 "왜 여기 있나" 참고)
│   └── response/                  외부 응답 매핑 record 4종
└── exception/BatchJobException    BbqService만 던지는 배치 실패 예외
```

잡 슬러그 7종: `grade` · `product` · `productsoldout` · `rank` · `region` · `reviewblind` · `search`. 잡별 UseCase·트리거 대응표는 `batch-module/AGENTS.md`에 있다.

## 왜 `crawling/bbq`가 이 모듈에 있나 (챕터 01 §2 판단 기록)

챕터 스펙은 "driven 클라이언트면 batch-module 잔류 + 인터페이스 분리가 원칙이나, 파일럿에서는 실제 참조 방향을 확인 후 최소 이동으로 처리하고 결정을 남긴다"고 했다. 확인 결과 **`crawling/bbq`는 driven 클라이언트가 아니라 application 계층 코드**였다.

- `BbqProductSyncService`는 `@Service @Transactional`로 **트랜잭션 경계를 소유**하고, 저장 불변식은 도메인 서비스 `ProductRegistrationService`에 위임하며, 동기화 대상 탐색은 `ProductQueryPort`(읽기 포트)로 한다 — 자기 Javadoc이 스스로를 "batch 전용 application 서비스"라고 부른다.
- `BbqService`는 오케스트레이션이고, **진짜 driven 클라이언트는 이미 `external-api`에 있다**(`external.crawling.bbq.BbqApiClient`, `external.file.RemoteImageDownloader`).
- `response/*`는 그 서비스의 경계 타입, `*Registration` 3종은 순수 데이터 record다.

`ProductSchedulerService`가 이 6개 타입을 직접 쓰므로, 남겨 두면 batch-application → batch-module 역방향 의존이 생겨 모듈 분리 자체가 성립하지 않는다. **`BatchJobException`은 `BbqService`만 던지므로 함께 이동**했다(잔류 파일 중 이 예외를 쓰는 것은 0건이었다).

그 결과 이 모듈은 `external-api`를 의존한다 — 챕터 스펙의 dependencies 블록에 없던 한 줄이다. 방향은 맞다(application → 외부 시스템 접근 계약).

## Dependencies

### Internal
- `domain-module` (implementation) — 도메인 모델·VO·write 포트·도메인 서비스
- `external-api` (implementation) — `BbqApiClient`·`RemoteImageDownloader` (위 crawling 판단의 귀결)

### External
- `spring-tx` (implementation) — **`@Transactional`만을 위한 최소 의존**. 이전에는 `infrastructure:persistence`의 `spring-boot-starter-data-jpa`를 타고 전이로 들어와 소스 어디에도 선언이 없었다. infra를 클래스패스에서 뺀 결과 이 숨은 의존이 드러났으므로 starter 통째가 아니라 실제로 쓰는 것만 명시한다(버전은 Boot BOM이 고정).
- Spring Boot Starter(루트 `subprojects`가 부여)

### infrastructure 의존 없음 — 이 모듈의 핵심

**`import com.tastyhouse.infrastructure...` 한 줄이 실제 컴파일 에러가 된다.** `domain-module`의 프레임워크-프리 컴파일 게이트와 같은 방식으로, 계층 규칙이 리뷰 규율이 아니라 빌드 게이트가 된다. ArchUnit `shouldNotDependOnInfrastructure`는 누군가 build.gradle에 의존 한 줄을 되돌리는 회귀를 막는 2차 방어선이다(컴파일은 통과하고 계층만 조용히 무너지는 경우).

## ArchUnit (`architecture/LayerRulesTest`)

batch-module에서 이동한 5개 + 물리 분리로 비로소 표현 가능해진 신설 2개.

| 규칙 | 내용 | 출처 | 상태 |
|---|---|---|---|
| `schedulerServicesShouldImplementUseCase` | `*SchedulerService`는 `..port.in..` 구현 (패턴에서 `application` 세그먼트 제거) | 이동 | **활성** |
| `responseRecordsShouldBeDomainAndInfraFree` | `..response..` ✗ domain/infra | 이동 | **활성**(domain은 클래스패스에 있어 실제로 실패 가능) |
| `applicationServicesShouldNotDependOnWebLayer` | `*SchedulerService` ✗ web 플럼빙 | 이동 | 휴면 |
| `inboundPortsShouldBeBoundaryTyped` | `..port.in..` ✗ domain(`domain.exception` carve-out)/infra/web | 이동 | 검사 표면 0 (아래) |
| `shouldNotDependOnQuerydsl` | ✗ `com.querydsl..` | 이동 | 휴면 |
| `shouldNotDependOnInfrastructure` | ✗ `com.tastyhouse.infrastructure..` — 이동하며 `..persistence..`에서 **infra 전체로 범위 확대** | 이동 | 휴면 |
| **`applicationMustBeServletFree`** | 모듈 전체 ✗ `jakarta.servlet..`·`org.springframework.web..` | **신설** | 휴면 |
| **`applicationMustNotDependOnAdapters`** | ✗ `com.tastyhouse.batch..` (역방향 의존 금지) | **신설** | 휴면 |

### ⚠️ "휴면"의 의미 — 8개 중 실제 강제는 2개다

**휴면 = 금지 대상 패키지가 이 모듈 클래스패스에 아예 없어, 위반 코드가 애초에 컴파일되지 않는 상태다.** 이 모듈에서 계층을 실제로 강제하는 것은 ArchUnit이 아니라 **build.gradle**이고(그것이 모듈 분리의 요점이다), 휴면 규칙은 누군가 의존 한 줄을 되돌리는 **회귀를 잡는 2차 방어선**이다.

이 구분을 표에 적어 두는 이유는, "규칙 8개"가 실제보다 넓은 커버리지로 읽히는 것을 막기 위해서다. **`RuleAnchorTest`는 이 문제를 잡지 못한다** — 그것이 세는 것은 "규칙이 대상 클래스를 갖는가"(공허 통과 방지)이지 "규칙이 실패할 수 있는가"(falsifiability)가 아니다.

**`inboundPortsShouldBeBoundaryTyped`는 휴면과도 다른 세 번째 상태다**: 대상 인터페이스 7개는 실재하지만 배치 잡은 입력이 없어 전부 `void foo()` 하나뿐이라, 의존 그래프에 잡힐 타입이 0건이다. 규칙과 `domain.exception` carve-out은 **UseCase가 처음 파라미터를 갖는 시점**을 위해 미리 세워 둔 것이므로, 지금 아무것도 걸리지 않는다고 죽은 코드로 보고 지우지 말 것.

`applicationMustBeServletFree`가 api 모듈과 다른 점: batch에는 HTTP 경계도 파일 업로드도 없어 **`MultipartFile` 예외가 필요 없다.** api 모듈이 달고 있는 그 carve-out 없이 완전한 servlet-free를 표현할 수 있고, 이것이 application 계층을 물리 분리해서 얻는 것 중 하나다. (다만 위 표대로 현재는 휴면이며, 좁은 쪽 `applicationServicesShouldNotDependOnWebLayer`를 사실상 포섭한다.)

**`allowEmptyShould(true)`는 쓰지 않는다.** 규칙이 대상을 잃으면 공허 통과를 열지 말고 규칙을 지우거나 anchor를 고친다 — 이번 분할에서 batch-module 쪽 규칙 4개를 **삭제**한 것이 그 선례다(대상 클래스가 전부 이 모듈로 떠났다).

### `RuleAnchorTest` — 공허 통과 자동 검출 (신설)

`noClasses().that()...`은 대상이 0건이어도 조용히 통과하므로, 위 원칙을 지켰는지가 사람 눈에만 의존한다. `RuleAnchorTest`가 각 규칙의 anchor 개수(`*SchedulerService` 7 · `..port.in..` 7 · response record 4 · 모듈 전체 ≥28)를 직접 세어, 클래스가 모듈 사이를 옮겨 다니다 대상이 통째로 사라지면 **빌드를 실패시킨다.** 잡을 추가·삭제하면 이 숫자도 함께 고친다.

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar(`security-module` 선례). 배치를 띄우는 것은 `batch-module`의 fat jar다.
- **빈 배선 실수는 빌드로 드러나지 않는다** — batch에는 `contextLoads` 테스트가 없어서 `@Import` 누락 시 빌드는 green이고 jar만 조용히 깨진다. 배선을 건드렸으면 실제로 띄워 `Started BatchApplication` 마커를 확인한다(`batch-module/AGENTS.md`의 명령 참고).

- **읽기 계약을 이 모듈이 소유한다 (챕터 09 — `application-common-module` 해체 완료)**: 이 앱만 소비하는 `{Ctx}QueryPort`·`*Result`·`*SearchCondition`은 `src/main/java/com/tastyhouse/application/<ctx>/port/out/`에 있다. 패키지가 모듈명과 어긋나는 것은 의도된 선택이며(`infrastructure:persistence`가 `com.tastyhouse.infrastructure..`를 쓰는 것과 같은 선례), 그 덕분에 계약이 어느 모듈로 가든 소비 측 import와 ArchUnit 패키지 규칙이 바뀌지 않는다.
  - **2개 이상의 앱이 쓰는 공유 계약은 여기 두지 않고 `domain-module`이 소유한다** — 다른 앱이 이 모듈을 의존하게 되는 앱 간 수평 의존을 막기 위해서다. 새 계약을 추가하기 전에 소비 앱이 몇 개인지 먼저 센다.
  - **프레임워크-프리를 `LayerRulesTest#readContractsShouldBeFrameworkFree`가 지킨다**: 이 모듈은 spring starter를 받으므로 `application-common-module` 시절의 컴파일 게이트가 없다. 계약이 참조해도 되는 것은 `java..`·`com.tastyhouse.domain..`과 자기 자신뿐이다.

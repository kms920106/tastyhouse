# ADR-0037: infrastructure 모듈의 자바 패키지 루트를 com.tastyhouse.infrastructure.{모듈}로 통일한다

- 상태: 채택
- 결정 커밋: `e03a0df33` (2026-10-04)
- 대체한 결정: —

## 맥락

`infrastructure`는 기술별로 나뉜 중첩 Gradle 프로젝트입니다(`infrastructure:jpa`, `infrastructure:redis`, `infrastructure:kakao-oauth` 등). 모듈 이름과 자바 패키지 이름은 별개라서, 모듈을 나누거나 옮겨도 패키지는 그대로 남을 수 있습니다.

- **패키지 루트**: 한 모듈의 main 클래스가 모두 들어가는 최상위 자바 패키지입니다.
- **컴포넌트 스캔**: Spring이 지정한 패키지 아래의 `@Component`류 클래스를 찾아 빈으로 등록하는 기능입니다. 하위 패키지도 함께 스캔됩니다.

결정 전에는 infrastructure 모듈의 패키지 루트가 세 갈래였습니다. 당시 persistence 모듈이 `com.tastyhouse.infrastructure`를 **통째로** 스캔했기 때문에, 다른 모듈을 그 아래 두면 의존하지도 않은 어댑터까지 스캔돼 부팅이 깨질 수 있었습니다. 그래서 벤더 모듈은 일부러 다른 루트(`com.tastyhouse.external`)에 살았습니다. 이 분산 때문에 `DomainPurityTest`·application `LayerRulesTest`가 금지하는 `com.tastyhouse.infrastructure..` 패턴이 벤더·restclient를 덮지 못하는 구멍도 있었습니다.

## 이전 규칙

### backend/CLAUDE.md — 모듈 지도 · 패키지 최상위 지도 · infrastructure 패키지 규칙

- `infrastructure:persistence`·`infrastructure:redis`는 `com.tastyhouse.infrastructure..`를 썼습니다. persistence는 모듈명 세그먼트 없이 컨텍스트를 루트에 바로 펼쳤습니다(예: `com.tastyhouse.infrastructure.order.persistence`). 재편은 Gradle 좌표와 디렉터리만 바꿨습니다.
- 벤더·채널 16모듈(`firebase`·`aws-s3`·`aws-ses`·`aws-sns`·`kakao-oauth`·`naver-oauth`·`apple-oauth`·`facebook-oauth`·`pg`·`tosspayments`·`mail`·`javamail`·`sms`·`solapi`·`bbq`·`admdongkor`)은 `com.tastyhouse.external..` 하나를 하위 패키지별로 나눠 썼습니다. `PersistenceModuleAutoConfiguration`의 `@ComponentScan("com.tastyhouse.infrastructure")` 범위 **밖**에 남아 있어야 했기 때문입니다.
- 코어 `infrastructure:restclient`는 모듈 리네임 때 `com.tastyhouse.restclient..`로 옮겨 빠졌고, 클래스는 전부 `.config` 하위에 있었습니다.
- persistence 스캔은 `@ComponentScan(basePackages = "com.tastyhouse.infrastructure", excludeFilters = REGEX redis)` — redis를 REGEX 제외 필터로 겨우 빼냈습니다.
- 패키지 최상위 지도의 `com.tastyhouse.external..` 시절 이력: 16개 벤더·채널 모듈이 하위 패키지를 나눠 소유했고, 모듈마다 덩어리가 겹치지 않아 split package가 아니었습니다. 그러려고 `external.file.firebase`→`external.firebase`, `external.file.s3`→`external.aws.s3`, `external.mail.ses`→`external.aws.ses`, `external.sms.sns`→`external.aws.sns`, `external.file.RemoteImageDownloader`→`external.crawling` 5건을 옮겼습니다(코어가 `external.file`을 스캔할 때 하위 패키지가 동반 스캔되는 것도 함께 막았습니다). 이후 분할로 `external.crawling`→`external.bbq`, `external.region`→`external.admdongkor`, messaging 4분할로 `external.mail.javamail`→`external.javamail`·`external.sms.solapi`→`external.solapi`(채널 스캔에 벤더가 동반 스캔되지 않게), `external.payment.toss`→`external.tosspayments`, `external.oauth.{kakao,naver,apple,facebook}`→`external.{kakao,naver,apple,facebook}.oauth`가 됐습니다. restclient의 `.exception` 하위 패키지는 존재한 적이 없습니다.
- 루트 통일 직후 before/after 표의 벤더 예시: `com.tastyhouse.external.kakao.oauth.KakaoTokenResponse` → `com.tastyhouse.infrastructure.kakao.oauth.dto.KakaoTokenResponse`, `com.tastyhouse.external.solapi.{request,response}.*` → `com.tastyhouse.infrastructure.solapi.dto.*`(둘 다 이후 [ADR-0040](0040-package-private-visibility.md)으로 루트 평면 package-private이 됐습니다).

### backend/CLAUDE.md — 코딩 스타일 (import 순서) > 자사 그룹 내부 계층 정렬

- 3순위(driven 어댑터)는 `infrastructure` — `external` · `restclient` 세 세그먼트였고, 같은 순위 안 순서는 `external` → `infrastructure` → `restclient`(ASCII 오름차순)였습니다. 루트 통일로 두 세그먼트가 사라져 3순위는 `infrastructure` 하나가 됐습니다. `backend/AGENTS.md`의 "Working In This Directory" import 순서 항목도 같은 서술이었습니다.

### CLAUDE.md · backend/AGENTS.md — 리포지토리 구조 · Module Dependency Graph

- 루트 `CLAUDE.md`는 벤더·채널 16모듈이 예외로 `com.tastyhouse.external..`을 유지하는 이유를 "`InfrastructureModuleConfig`(챕터 02 리네임 전 이름)가 `com.tastyhouse.infrastructure`를 통째로 스캔해서, 그 아래로 옮기면 admin/ceo/batch 부팅이 깨진다"고 적었습니다.
- `backend/AGENTS.md`는 "모듈명과 자바 패키지명은 다를 수 있다: 벤더 13 + 채널 3 = 16모듈이 `com.tastyhouse.external..`을 나눠 소유하고, 코어 `restclient`는 `com.tastyhouse.restclient..`를 소유한다"고 적었습니다. 루트 통일 직후 같은 자리는 "코드가 있는 16모듈(`persistence`·`redis`·`restclient` + 벤더 13)"으로 바뀌었습니다(이후 jpa 모듈 분리로 17모듈).

### backend/infrastructure/jpa/AGENTS.md — 머리말 · 규칙 · 봉인·가드 목록 (당시 infrastructure:persistence)

- 챕터 05 경로 이동 시점에 자바 패키지 `com.tastyhouse.infrastructure..`는 불변이었고, 루트 통일로 `com.tastyhouse.infrastructure.persistence..`로 한 겹 내려갔습니다. 루트를 모듈명으로 내리면서 통째 스캔과 redis 제외 필터가 함께 사라졌습니다.
- `LayerRulesTest` 패키지 술어는 루트를 끝까지 적도록 바뀌었습니다. 루트 자체에 `persistence` 세그먼트가 생겨, 이전 술어를 두면 조회 DAO(`com.tastyhouse.infrastructure.persistence.<ctx>.query`)까지 write 어댑터로 매칭되기 때문입니다.

| 술어 | before (루트 `com.tastyhouse.infrastructure`) | after (루트 `com.tastyhouse.infrastructure.persistence`) |
|---|---|---|
| write 어댑터 | `com.tastyhouse.infrastructure..persistence..` | `com.tastyhouse.infrastructure.persistence..persistence..` |
| 조회 DAO | `com.tastyhouse.infrastructure..query..` | `com.tastyhouse.infrastructure.persistence..query..` |
| 모듈 루트 | 없음 | `com.tastyhouse.infrastructure.persistence..`(`shouldResideInModuleRootPackage`) |
| 앱 모듈 `LayerRulesTest`의 persistence 금지 술어 | `"..infrastructure..persistence.."` | `"com.tastyhouse.infrastructure.persistence.."` |

- 봉인: "중복처럼 보인다"며 `com.tastyhouse.infrastructure..persistence..`로 줄이지 않는다 — `noClasses()`는 대상이 늘든 줄든 위반이 없으면 통과하므로 컴파일·테스트 어느 쪽도 실패하지 않은 채 대상이 바뀝니다. 대상 메서드는 `queryShouldNotDependOnDomain`·`persistenceShouldNotDependOnQuery`·`sealedPersistenceToQueryShouldNotBeStale`·`shouldResideInModuleRootPackage`입니다. 이후 [ADR-0047](0047-jpa-module-split.md)로 루트가 `com.tastyhouse.infrastructure.jpa`가 되어 이 함정은 해소됐습니다.

### backend/infrastructure/restclient/AGENTS.md — 패키지 봉인 절

- "자바 패키지 `com.tastyhouse.external..` 봉인" 절은 벤더 모듈을 `com.tastyhouse.infrastructure` 아래로 옮기면 persistence 스캔이 그 트리를 통째로 훑어 **admin-api·ceo-api·batch-module의 부팅이 깨진다**고 적고, 모듈 디렉터리와 패키지 이름이 어긋나 보여도 정리하지 말라고 봉인했습니다.
- 코어를 `com.tastyhouse.infrastructure.restclient`로 두지 않는 이유로, persistence 스캔이 진입 설정을 다시 등록하는 이중 등록과 `@AutoConfiguration` 순서·조건 계약 무력화를 들었습니다. Spring은 같은 클래스의 중복 스캔을 한 번만 등록하므로 기동 실패로 이어지지는 않지만 등록 주체가 persistence 쪽으로 넘어간다는 것이었고, 이 이동은 `backend/CLAUDE.md`의 "후속 프로그램 — 벤더 패키지를 `com.tastyhouse.infrastructure.*`로 정렬" 절의 stage B 대상(벤더·redis와 함께)으로 미뤄져 있었습니다. 이 ADR이 그 stage A·B를 실행한 것입니다.
- "코어 패키지는 `com.tastyhouse.restclient..`, 벤더 9모듈은 `com.tastyhouse.external..` 유지" 절: 코어만 `security-core`/`security-module`이 `com.tastyhouse.security..`를 공유하는 선례(모듈명 ≠ 패키지명)를 따라 모듈 리네임과 함께 `com.tastyhouse.restclient.config`로 옮겼습니다. 코어는 persistence 스캔 트리에 걸리는 벤더 빈이 없어(설정 클래스뿐) 그 제약에서 자유로웠습니다. 7모듈 분리 전에는 persistence 스캔이 진입 설정의 OAuth REGEX 제외 필터를 우회해 admin/ceo/batch가 `Could not resolve placeholder 'apple.team-id'`로 부팅에 실패한 적이 있었습니다. 문서들은 벤더 수를 "9모듈"로 적기도 했습니다(실제 대상은 벤더·채널 16모듈).
- 하위 문서 `src/main/java/.../restclient/AGENTS.md`(루트 통일 전 `src/main/java/com/tastyhouse/restclient/AGENTS.md`)는 코어 패키지에 `config/` 하위 패키지 하나만 남는다고 적었고, 같은 봉인("벤더 패키지를 `com.tastyhouse.infrastructure` 아래로 옮기지 않는다")을 반복했습니다. 가드 `NoReactiveHttpClientTest`도 `com.tastyhouse.restclient.architecture`에서 `com.tastyhouse.infrastructure.restclient.architecture`로 옮겨지며 자기 자신을 예외로 지정하는 `GUARD_SOURCE` 경로 문자열이 함께 바뀌었습니다.
- "벤더 패키지를 `external.file` 아래에 두지 않았던 이유" 절: 7모듈 분리 당시 코어 스캔이 `com.tastyhouse.external.file`이어서 `external.file.firebase`·`external.file.s3`를 그대로 두면 코어만 import해도 벤더 빈이 딸려 올라왔습니다. 그래서 firebase·s3·`RemoteImageDownloader` 3건만 패키지를 바꿨고, 파일 저장 SPI 삭제로 그 스캔이 사라진 뒤에도 split package 회피를 위해 되돌리지 않았습니다.

### backend/infrastructure/redis/AGENTS.md — 패키지 루트 `com.tastyhouse.infrastructure.redis`

- 이 모듈의 패키지는 루트 통일 전후로 바뀌지 않았습니다. 바뀐 것은 persistence 스캔입니다: before는 `@ComponentScan("com.tastyhouse.infrastructure")`가 이 모듈 트리까지 덮고 REGEX `excludeFilters`(`com\.tastyhouse\.infrastructure\.redis\..*`)로 되돌렸습니다. 제외 필터가 빠지면 `RedisModuleAutoConfiguration`과 중복 등록되는 구조였습니다. after는 `@ComponentScan("com.tastyhouse.infrastructure.persistence")`로 겹치지 않아 제외 필터가 삭제됐고 Redis 빈 등록 주체가 하나라는 것이 구조적으로 보장됐습니다.

### 벤더 모듈 AGENTS.md(bbq·firebase·admdongkor·aws-s3·aws-ses·aws-sns·kakao/naver/apple/facebook-oauth·javamail·solapi·tosspayments)

각 문서에 "자바 패키지 `com.tastyhouse.external.{벤더}` 봉인" 절이 있었습니다. 외부 연동 모듈이 `com.tastyhouse.external..`을 유지하는 것은 취향이 아니라 스캔 범위 제약이라고 적었습니다.

모듈별로 덧붙은 내용은 다음과 같습니다.

- **kakao·naver·apple·facebook-oauth**: 이 패키지 이름은 web-api ArchUnit `LayerRulesTest#shouldDependOnOauthSpiOnlyNotProviderPackages`가 문자열로 참조하므로, 이름을 바꾸면 규칙이 조용히 대상을 잃는다고 적었습니다. 루트 통일 때 그 목록을 `com.tastyhouse.infrastructure.{kakao,naver,facebook,apple}.oauth..`로 교체했습니다.
- **javamail·solapi·aws-ses·aws-sns·tosspayments**: 옛 위치(`external.mail.javamail`·`external.sms.solapi`·`external.mail.ses`·`external.sms.sns`·`external.pg.toss`)로 되돌리면 채널 모듈의 스캔(예: `infrastructure:mail`의 `@ComponentScan("com.tastyhouse.external.mail")`)에 동반 스캔·등록된다는 이유를 함께 적었습니다.
- **firebase·aws-s3**: 원래 위치 `external.file.firebase`·`external.file.s3`로 되돌리지 않는다고 적었습니다. 분리 당시에는 코어 `ExternalModuleAutoConfiguration`의 `com.tastyhouse.external.file` 스캔에 동반 스캔되는 것이 사유였고, 그 스캔이 없어진 뒤에도 벤더 모듈마다 겹치지 않는 하위 패키지를 소유해 split package를 피하기 위해 이동을 금지했습니다.
- **admdongkor**: 분리 때 `com.tastyhouse.external.region`에서 `com.tastyhouse.external.admdongkor`로 바뀌었고(모듈명과 맞춤 — `external.file.firebase → external.firebase` 선례), 클래스명·프로퍼티 접두사·환경변수 이름은 불변이었습니다. 옛 위치 `external.region`으로 되돌리지 않는다고 적었습니다.
- **bbq**: 옛 위치 `external.crawling`으로 되돌리지 않는다고 적었습니다. `RemoteImageDownloader`는 `external.file.RemoteImageDownloader` → `external.crawling.RemoteImageDownloader` → `external.bbq.RemoteImageDownloader` → `com.tastyhouse.infrastructure.bbq.RemoteImageDownloader`로 옮겨졌습니다. 첫 이동은 당시 코어가 `external.file`을 스캔해 admin/ceo에도 이 빈이 동반 스캔됐기 때문이고, 두 번째는 소유 모듈의 패키지에 두는 규칙을 따른 것입니다.

## 결정

persistence를 `com.tastyhouse.infrastructure.persistence`로 한 겹 내리고 스캔을 자기 루트로 좁혔습니다. 그러자 제약이 사라져, 코드가 있는 infrastructure 모듈 전부의 루트를 `com.tastyhouse.infrastructure.{Gradle 모듈명의 하이픈을 점으로}` 하나로 맞췄습니다.

| 항목 | before | after |
|---|---|---|
| persistence 패키지 | `com.tastyhouse.infrastructure.order.persistence` | `com.tastyhouse.infrastructure.persistence.order.persistence` (이후 [ADR-0047](0047-jpa-module-split.md)로 `com.tastyhouse.infrastructure.jpa.order.persistence`) |
| persistence 스캔 | `@ComponentScan(basePackages = "com.tastyhouse.infrastructure", excludeFilters = REGEX redis)` | `@ComponentScan("com.tastyhouse.infrastructure.persistence")` |
| redis | `com.tastyhouse.infrastructure.redis` | 불변 |
| restclient | `com.tastyhouse.restclient.config.RestClientConfig` | `com.tastyhouse.infrastructure.restclient.RestClientConfig` (`.config` 하위 패키지 소멸) |
| 벤더 13모듈 | `com.tastyhouse.external.{벤더}` (예: `external.kakao.oauth`) | `com.tastyhouse.infrastructure.{벤더}` (예: `infrastructure.kakao.oauth`) |
| 벤더 wire DTO | 벤더마다 제각각(solapi는 `request/`·`response/`) | `{루트}.dto`로 모음 (이후 [ADR-0040](0040-package-private-visibility.md)이 루트 평면으로 다시 옮김) |
| 가드 | 없음 | 모듈마다 `shouldResideInModuleRootPackage` (redis·restclient는 `PackageRootTest`, 벤더는 `VendorLayerRulesTest`, persistence는 `LayerRulesTest`) |

## 근거

- 세 갈래 루트의 유일한 이유는 persistence의 통째 스캔이었습니다. 스캔을 자기 루트로 좁히면 그 이유가 사라집니다(커밋 메시지: "persistence를 infrastructure.persistence로 내려 스캔을 자기 루트로 좁히고 redis 제외 필터를 삭제했다").
- 루트를 통일하면 domain·application의 `com.tastyhouse.infrastructure..` 금지 가드가 벤더까지 덮습니다. 이전에는 벤더·restclient가 그 패턴 밖에 있어 구멍이었습니다.
- 모듈명에서 패키지를 기계적으로 유도할 수 있어, 모듈 디렉터리와 패키지가 어긋나 보이는 상태가 없어집니다.

## 결과·감수한 손실

- **런타임 동작은 바뀌지 않았습니다.** JPQL·yml·logback·AOP pointcut·Redis 직렬화에 이 FQN 문자열이 없습니다. `@EnableConfigurationProperties`로 등록된 Properties 빈 이름(`{prefix}-{FQN}`)은 바뀌지만 문자열로 참조하는 곳이 없습니다.
- 벤더 패키지 세그먼트(`.firebase`·`.aws.s3`·`.aws.ses`·`.aws.sns`·`.bbq` 등)는 그대로이고 접두어만 `com.tastyhouse.external` → `com.tastyhouse.infrastructure`로 바뀌었습니다. 모듈마다 겹치지 않는 하위 패키지를 소유해 split package를 피하는 구조도 그대로입니다.
- 이 직후 persistence 루트에 `persistence` 세그먼트가 생겨 `persistence.order.persistence`처럼 이름이 두 번 나오는 함정이 있었습니다. 이것은 [ADR-0047](0047-jpa-module-split.md)이 루트를 `jpa`로 바꾸면서 해소됐습니다.
- `{루트}.dto` 배치는 같은 날 [ADR-0040](0040-package-private-visibility.md)이 루트 평면 배치로 바꿨습니다.

## 영향받은 문서

- `CLAUDE.md` · 리포지토리 구조 · 코드가 있는 infrastructure 모듈은 `com.tastyhouse.infrastructure.{모듈명}` 하나를 루트로 쓴다
- `backend/CLAUDE.md` · infrastructure 패키지 규칙 (루트 = 모듈명) · 루트 규칙 전문과 `shouldResideInModuleRootPackage` 가드
- `backend/CLAUDE.md` · 패키지 최상위 지도 · `com.tastyhouse.infrastructure.{벤더}..` 행
- `backend/CLAUDE.md` · "infrastructure 패키지 규칙 (루트 = 모듈명)" · 벤더까지 루트 `com.tastyhouse.infrastructure.{모듈}`로 정렬된 현재 규칙
- `backend/infrastructure/restclient/AGENTS.md` · 봉인·가드 목록 > 자바 패키지 루트는 `com.tastyhouse.infrastructure.{모듈}`이다 · 코어·벤더 루트 봉인
- `backend/infrastructure/restclient/AGENTS.md` · 벤더 패키지를 `external.file` 아래에 두지 않는 이유 (패키지 예외 3건) · 세그먼트 보존 경위
- `backend/infrastructure/jpa/AGENTS.md` · 봉인·가드 목록 > `LayerRulesTest` 패키지 술어 — 루트 `com.tastyhouse.infrastructure.jpa`를 끝까지 적는다
- `backend/infrastructure/{bbq,firebase,admdongkor,aws-s3,aws-ses,aws-sns,kakao-oauth,naver-oauth,apple-oauth,facebook-oauth,javamail,solapi,tosspayments}/AGENTS.md` · "봉인·가드 목록" > "자바 패키지 `com.tastyhouse.infrastructure.{벤더}` 봉인" · 벤더 13모듈 — 루트 `com.tastyhouse.infrastructure.{벤더}` 고정

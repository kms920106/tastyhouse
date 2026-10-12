# ADR-0018: 외부 연동은 코드 없는 채널 스타터 + 벤더 구현 모듈로 세분한다

- 상태: 채택
- 결정 커밋: `ae9870cd4` (2026-09-05)
- 대체한 결정: —

## 맥락

[ADR-0016](0016-external-vendor-channel-split.md)으로 외부 연동이 벤더·채널별 모듈로 나뉘었지만, 파일 저장을 쓰려면 앱마다 코어 SPI(`:infrastructure:external`)와 벤더 구현(`:infrastructure:firebase`)을 **둘 다** `build.gradle`에 선언하고, `spring.config.import`에도 `application-external.yml`·`application-firebase.yml`을 둘 다 나열해야 했다. 같은 벤더 결정이 4앱 × 2곳 = 8곳에 복제됐고, 앱이 "Firebase로 저장한다"는 구현 선택을 알고 있었다.

- **스타터(starter)**: 자바 코드 없이 "무엇을 조립하는가"만 선언하는 Gradle 모듈. `spring-boot-starter-data-redis`가 Lettuce를 고르는 것과 같은 형태.

이 ADR은 번복이 아니라 세분화를 묶는 우산 결정이다. 루트 `CLAUDE.md`의 모듈 수 연대기도 여기에 기록한다.

## 이전 규칙

앱이 한 기능을 이루는 모듈(코어 SPI + 벤더 구현)과 그 yml을 직접 나열한다.

채널 모듈은 등록 코드를 가질 수 있었다 — messaging(이후 `mail`·`sms`)은 `MailDomainConfig`·`SmsDomainConfig`를, payment(이후 `pg`)는 `PgGatewayConfig`(라우터 등록)를 가졌다. 그래서 "채널(등록 코드 있음) vs 스타터(코드 없음)"로 나뉘어 있었다.

### `backend/infrastructure/restclient/AGENTS.md` — 어디로 갔는지 (포인터) — 형제 모듈 수 연대기

당시 서술은 `infrastructure` 아래 driven 어댑터 12개에 챕터 03에서 신설된 `infrastructure:file-storage`가 더해져 13개가 됐고, 이 하나만 어댑터가 아니라 자바 코드 없는 조립 전용 스타터라고 적었다. 이후 분할로 21개가 됐고, 소셜 로그인 채널 `infrastructure:oauth`도 채널·벤더 분할로 같은 형태가 되어 스타터는 `file-storage`·`oauth` 둘이라고 적었다(지금은 조립 6모듈 — 아래 결과 참고).

### `backend/infrastructure/redis/AGENTS.md` — 신설 배경 (챕터 05) — 형제 수 연대기

챕터 05 재편 후 `infrastructure:external`이 편입돼 driven 어댑터 3형제(`persistence`·`redis`·`external`)가 됐고, external 기술별 분할과 `aws` 3분할로 `persistence`·`redis`·`external`·`firebase`·`aws-s3`·`aws-ses`·`aws-sns`·`oauth`·`payment`·`messaging`·`bbq`·`admdongkor` 12형제가 됐다. 이후 `messaging` 4분할(2026-09-26)로 15형제(코어 `external`은 `restclient`로 리네임), `payment`가 `pg`·`tosspayments`로 나뉘어 16형제, `oauth` 채널 스타터화와 벤더 4종 신설(2026-09-27)로 19형제가 됐다. 코드 없는 스타터 `file-storage`·`oauth`는 어댑터가 아니라 형제 수에서 뺐다.

### `backend/security-module/AGENTS.md` — Working In This Directory — 새 관심사 배치 판단 기준

판단 기준 제목에 "챕터 05 개정, 챕터 03으로 세분화" 꼬리표가 붙어 있었고, 외부 연동 어댑터 목록에서 코드 없는 조립 스타터는 `file-storage`·`oauth` 둘만 "어댑터를 갖지 않는다"고 적었다(당시 `pg`·`mail`·`sms`는 등록 코드가 있는 채널로 어댑터 목록에 들어 있었다).

## 결정

**한 기능을 이루는 모듈 여러 개를 앱이 나열하지 않는다 — 조립 전용 스타터 모듈이 묶어서 노출하고, 앱은 그 스타터 하나만 의존한다.**

| 항목 | before | after |
|---|---|---|
| 앱 `build.gradle`(파일 저장) | external + firebase 2줄 | `runtimeOnly project(':infrastructure:file-storage')` 1줄 |
| 앱 yml import | `application-external.yml` + `application-firebase.yml` | `classpath:application-file-storage.yml` 1줄 |
| `file.provider` 키 | `application-external.yml` | 스타터의 `application-file-storage.yml` |
| 스타터의 자바 코드 | — | 없음(`compileJava` NO-SOURCE, jar에는 yml만) |
| 컴파일 클래스패스 | — | 벤더 없음 — `runtimeOnly`는 `runtimeElements`로 전이되어 `runtimeClasspath`에만 실린다 |

적용 판단 기준: 여러 앱이 같은 조합(교체 가능한 벤더 구현과 그 설정)을 반복 선언하면 스타터 후보다. 앱마다 조합이 다르거나 교체 가능한 벤더가 하나뿐이면 만들지 않는다.

같은 패턴의 후속 분할(번복이 아니라 세분화라 ADR을 따로 만들지 않았다):

| 커밋 | 내용 | 모듈 수 |
|---|---|---|
| `ae9870cd4` | 파일 저장 스타터 `infrastructure:file-storage` 신설 | 20 → 21 |
| (`15e76e263`, [ADR-0022](0022-aws-module-three-way-split.md)) | `infrastructure:aws` → `aws-s3`·`aws-ses`·`aws-sns` | 21 → 23 |
| `cd4ef5262` | `infrastructure:crawling` → `bbq`·`admdongkor` | 23 → 24 |
| `b02f83ecc` | `infrastructure:external` → `infrastructure:http-client`를 거쳐 `infrastructure:restclient` 리네임, WebClient/webflux 제거·`RestClient` 통일, `ExternalApiException`/`ExternalApiErrorCode` 해체 | 24(불변 — 리네임과 HTTP 클라이언트 전환·예외 계약 해체는 모듈 경계를 바꾸지 않는다) |
| `ed5de7d27` | `infrastructure:messaging` → 채널 `mail`·`sms` + 벤더 `javamail`·`solapi` | 24 → 27 |
| `3829c07f7` | `infrastructure:payment` → 채널 `infrastructure:pg` 리네임 + 벤더 `tosspayments` 신설, PG 라우터 | 27 → 28 |
| `33651f12b` | `infrastructure:oauth`를 코드 없는 채널 스타터로, 벤더 `kakao-oauth`·`naver-oauth`·`apple-oauth`·`facebook-oauth` 신설 | 28 → 32 |

## 근거

- 앱은 "파일을 저장한다"까지만 알고 "Firebase로"는 모른다 — 헥사고날 경계와 맞는다.
- 벤더 전환이 앱 무수정, 스타터 두 파일(`build.gradle` 한 줄 + yml 한 줄) 수정으로 끝난다.
- 스타터에 코드를 넣으면 조립 선언과 구현이 섞여 존재 이유가 사라진다.
- 조립 대상이 하나여도 스타터를 유지한다 — 앱이 벤더 이름을 모른다는 목적은 조립 대상 개수와 무관하고, 교체 가능한 벤더(firebase·s3)가 둘이다.

## 결과·감수한 손실

- 동작 변경 없음(커밋 메시지).
- 공존형 채널은 배타 선택(`@ConditionalOnProperty`)이 아니라 공존+라우팅을 택했다 — PG(`PgPaymentGatewayRouter`), 소셜 로그인(4종 항상 함께 조립).
- "채널 모듈은 등록 코드를 갖는다"는 부분은 이후 뒤집혔다 — 등록 코드가 application으로 옮겨가 `pg`·`mail`·`sms`도 코드 없는 스타터가 됐다([ADR-0026](0026-vendor-ports-to-application.md)). 지금 조립 6모듈(`persistence`·`file-storage`·`oauth`·`pg`·`mail`·`sms`)은 전부 자바 코드가 없다.
- 이후 모듈 수 변화(32 → 36 → 37 → 38 → 39)는 각각 [ADR-0036](0036-app-marker-removal.md)·[ADR-0045](0045-banner-mybatis-provider-pilot.md)·[ADR-0047](0047-jpa-module-split.md)·[ADR-0051](0051-mysql-module-split.md).

## 영향받은 문서

- `backend/CLAUDE.md` · "모듈 등록 컨벤션 (auto-configuration — 챕터 02)" > "벤더 선택은 앱이 아니라 스타터 모듈이 한다 (챕터 03)" · 스타터 컨벤션 본문
- `backend/CLAUDE.md` · 같은 절 > "공존형 채널 — PG 라우터 (`infrastructure:pg`)" · "공존형 채널 — 소셜 로그인 스타터 (`infrastructure:oauth`)" · 공존형 채널의 조립 방식
- `backend/infrastructure/file-storage/AGENTS.md` · "왜 있는가" · "⚠️ 이 모듈에 코드를 넣지 않는다" · 스타터의 존재 이유와 금지
- `backend/infrastructure/pg/AGENTS.md` · "이 모듈이 하는 일 — 조립과 설정 두 가지만" · "봉인·가드 목록" > "이 모듈에 코드를 넣지 않는다" · 채널 스타터 형태
- `backend/infrastructure/restclient/AGENTS.md` · "외부 연동 모듈 지도" · 코어·벤더·스타터 배치
- `CLAUDE.md` · "리포지토리 구조" · 현재 모듈 수와 조립 6모듈

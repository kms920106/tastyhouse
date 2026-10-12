# ADR-0021: external 코어의 파일 저장 SPI를 지우고 벤더가 FileStoragePort를 직접 구현한다

- 상태: 채택
- 결정 커밋: `13127bc9f` (2026-09-26)
- 대체한 결정: [ADR-0016](0016-external-vendor-channel-split.md)(일부 — 7모듈로 나눌 때 코어에 둔 파일 저장 SPI)

## 맥락

**SPI**(Service Provider Interface)는 코어 모듈이 정의하고 벤더 모듈이 구현하는 확장 지점 인터페이스입니다. **`FileStoragePort`**는 "파일을 저장한다"는 벤더 무관 계약(출력 포트)입니다.

`infrastructure:external`을 7모듈로 나눌 때([ADR-0016](0016-external-vendor-channel-split.md)) 파일 저장은 "코어 SPI + 교체 가능한 벤더 구현" 형태가 됐습니다. 코어의 `com.tastyhouse.external.file` 패키지에는 다음 셋이 있었습니다.

- `FileStorageStrategy` — 벤더 전략 인터페이스
- `FileStoragePortAdapter` — 도메인 포트 `FileStoragePort`를 구현하며 전략에 위임
- `FileStorageProperties` — `file.*` 설정 바인딩

`infrastructure:firebase`·`infrastructure:aws`(현 `aws-s3`)가 `FileStorageStrategy`를 구현했습니다. 같은 분리에서 전략 시그니처가 `MultipartFile` → `byte[]`로 바뀌었는데, 그러자 이 중간 두 겹이 아무 일도 하지 않게 됐습니다.

## 이전 규칙

### backend/infrastructure/restclient/AGENTS.md — (당시) 코어 모듈 서술

코어(당시 `infrastructure:external`)가 파일 저장 SPI 3종(`FileStorageStrategy`·`FileStoragePortAdapter`·`FileStorageProperties`)을 소유했다. 벤더 모듈은 그 전략을 구현했다. 그래서 `infrastructure:firebase`도 코어를 의존했다.

- `FileStoragePortAdapter`는 당시 도메인 포트 `com.tastyhouse.domain.file.port.FileStoragePort`를 구현했고, 그래서 코어의 Internal 의존에 그 포트가 걸려 있었다.
- 7모듈 분리 때 전략 시그니처가 `MultipartFile` → `byte[]`로 바뀌며 `ByteArrayMultipartFile` 래퍼가 사라지고 코어의 `spring-web` 의존이 끊겼다.
- 코어의 `application-external.yml`은 `file.provider` 한 줄만 담고 있었고, 챕터 03에서 그 값의 소유가 스타터의 `application-file-storage.yml`로 옮겨가며 삭제됐다. 바인딩 대상 `FileStorageProperties`(`file.*`)는 그 뒤에도 코어에 남아 있다가 이 결정에서 삭제됐다.
- 코어 진입 설정은 `ExternalModuleAutoConfiguration`(이후 `HttpClientModuleAutoConfiguration`을 거쳐 `RestClientModuleAutoConfiguration`으로 개명)이었고, 코어 전체를 스캔하며 `@EnableConfigurationProperties`를 가졌다.

### backend/CLAUDE.md — 벤더 선택은 앱이 아니라 스타터 모듈이 한다 (챕터 03)

스타터 `infrastructure:file-storage`는 신설 당시 코어 SPI(당시 `infrastructure:external`)와 벤더 구현(`infrastructure:firebase`) **둘을** `runtimeOnly`로 묶어 노출했다. 스타터 후보 판단 기준에도 "교체 가능한 벤더 구현과 그 설정, 그리고 코어 SPI"를 반복 선언하는 경우가 들어 있었다.

### backend/infrastructure/file-storage/AGENTS.md — 왜 있는가

이 모듈은 코어 SPI와 벤더 구현 둘을 조립하는 모듈이었다.

## 결정

코어의 `com.tastyhouse.external.file` 패키지(3개 타입)를 삭제합니다. 벤더 구현이 `FileStoragePort`를 **직접 구현**하도록 바꿔, 다른 driven 어댑터(`MailSenderPort`·`SmsSenderPort`·`PgPaymentGatewayPort`)와 같은 형태로 통일합니다.

| 항목 | before | after |
|---|---|---|
| 파일 저장 계약 | 코어 `FileStorageStrategy` + 위임 어댑터 `FileStoragePortAdapter` | 포트 `FileStoragePort` 하나 |
| 벤더 구현 | `FirebaseFileStorage`·`S3FileStorage`가 `FileStorageStrategy` 구현 | 두 클래스가 `implements FileStoragePort` |
| `FileStorageProperties`(`file.*`) | 코어에 존재(주입처 0건) | 삭제. `file.provider`는 벤더 구현의 `@ConditionalOnProperty` 문자열로만 소비 |
| 코어 auto-configuration 스캔 | 코어 전체 | `config` 패키지 하나, `@EnableConfigurationProperties` 제거 |
| `infrastructure:firebase` 의존 | 코어 + domain + firebase-admin | domain + firebase-admin(코어 의존 제거) |
| 스타터 `file-storage` 조립 대상 | 코어 + firebase | firebase 하나 |
| admin-api·ceo-api 런타임 클래스패스 | 코어(external)·webflux 포함 | 둘 다 없음 |
| 벤더 빈 선택 | — | `@ConditionalOnProperty(file.provider)` 배타 선택이라 `FileStoragePort` 빈은 항상 하나 |

## 근거

- `byte[]` 전환 뒤 `FileStorageStrategy`의 메서드 3개는 `FileStoragePort`와 **시그니처가 완전히 같았고**, `FileStoragePortAdapter`는 변환 없이 위임만 했습니다. `FileStorageProperties`는 주입처가 0건인 죽은 코드였습니다.
- 포트가 이미 벤더 무관 계약이므로, 벤더 모듈은 그 포트를 소유한 모듈만 의존하면 됩니다.
- 검토하고 채택하지 않은 대안은 다음 두 가지입니다.
  - **SPI를 `infrastructure:file-storage`로 옮긴다**: file-storage가 firebase를 `runtimeOnly`로 의존하는데, firebase가 SPI를 구현하려면 file-storage를 의존해야 합니다. file-storage ↔ firebase 순환이 됩니다.
  - **SPI 전용 모듈을 신설한다**: 포트와 모양이 같은 인터페이스 하나를 위해 모듈을 늘리는 것이라 얻는 것이 없습니다.

## 결과·감수한 손실

- 코어는 `config/`만 남은 HTTP 코어가 됐습니다(이후 `infrastructure:restclient`로 리네임). 파일 저장만 쓰는 admin·ceo의 런타임 클래스패스에서 코어가 빠졌습니다. web은 oauth·pg·메일·SMS 경로로, batch는 수집 모듈 경로로 코어를 계속 받습니다.
- 당시 `infrastructure:aws`는 SES/SNS 실패를 던지느라 코어 의존을 유지했습니다(이후 [ADR-0022](0022-aws-module-three-way-split.md) 3분할로 `aws-s3`만 코어 의존이 없어짐).
- 벤더 모듈 분리(firebase·aws), 모듈 수 21, 앱 설정은 바뀌지 않았습니다. API·동작 변경도 없습니다.
- 반증으로 확인했습니다. 구현 빈이 없으면 `FileStoragePort`를 주입받는 빈(`FileUrlResolver`)이 기동 시 실패합니다. 2026-09-26에 `--file.provider=s3`로 admin-api를 aws-s3 모듈 없이 띄우면 `FileUrlResolver`에서 `APPLICATION FAILED TO START`가 났습니다.
- 당시 `infrastructure:persistence`에서 `FileStoragePort`를 주입받는 빈은 `FileUrlResolver`와 `FileDomainConfig`(`FileUploadService` 등록)였습니다. `FileUploadService` 등록은 이후 덩어리 02-vendor-ports로 application의 `FileServiceConfig`로 옮겨졌다가, application `*ServiceConfig` 삭제로 클래스에 `@SharedApp` 마커만 달아 스캔 등록하는 방식이 됐습니다(그 마커는 이후 [ADR-0036](0036-app-marker-removal.md)에서 제거).
- 코어 진입 설정의 스캔은 `com.tastyhouse.restclient.config` 하나로 줄었습니다(지금 패키지 루트는 `com.tastyhouse.infrastructure.restclient`).
- "포트 시그니처에 `MultipartFile`·SDK 타입·`RestClient` 타입을 누출하지 않는다"는 원칙은 그대로입니다. 이후 RestClient 전환으로 `spring-web`은 코어의 정식 `api` 의존으로 되돌아왔지만, 그것은 HTTP 클라이언트 용도이지 파일 저장 시그니처 때문이 아닙니다.
- 스타터의 조립 대상은 하나로 줄었지만 스타터는 유지합니다. 앱이 벤더 이름을 모르게 한다는 목적은 조립 대상 개수와 무관하고, 교체 가능한 벤더는 여전히 firebase·s3 둘이기 때문입니다.
- `FileStoragePort` 자체는 이후 [ADR-0026](0026-vendor-ports-to-application.md)에서 `domain`을 떠나 `application`(`com.tastyhouse.application.file.port.out`)으로 옮겨졌습니다. 벤더 모듈의 의존도 `domain` → `application`으로 바뀌었습니다.

## 영향받은 문서

- `backend/infrastructure/restclient/AGENTS.md` · "파일 저장 SPI를 두지 않는 이유" · 전략 인터페이스와 위임 어댑터가 포트와 같은 모양이라 삭제했고, 벤더가 포트를 직접 구현하며, 대안 2개는 순환·무익으로 채택하지 않았다
- `backend/infrastructure/restclient/AGENTS.md` · "Dependencies" · 이 모듈은 파일 저장 포트를 구현하지 않는다
- `backend/infrastructure/restclient/src/main/java/com/tastyhouse/infrastructure/restclient/AGENTS.md` · "Packages" · 코어 패키지에는 파일 저장 타입이 없다
- `backend/infrastructure/file-storage/AGENTS.md` · "왜 있는가" / "구성" · 조립 대상은 firebase 하나이고, 순환이 없다
- `backend/CLAUDE.md` · "벤더 선택은 앱이 아니라 스타터 모듈이 한다 (챕터 03)" · 스타터는 벤더 구현만 묶고, 조립 대상이 하나여도 스타터를 유지하는 이유

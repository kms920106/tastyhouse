<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# external (integration adapters)

## Purpose
`domain-module`이 `<ctx>/domain/port/`에 선언한 출력 포트(`mail/`의 `MailSender`, `sms/`의 `SmsSender`, `FileStoragePort`·`PgPaymentGateway` 등)를 구현하는 외부 시스템 연동 어댑터. 각 제공자(provider)별로 격리된 패키지 구조로 관심사를 분리하며, 외부 API 응답 DTO가 도메인 모델로 유입되지 않도록 차단한다.

## Adapter Packages
| Package | Provider / Purpose |
|---------|-------------------|
| `oauth/{kakao,naver,apple,facebook}` | 소셜 로그인 — OAuth 토큰 발급/검증, 사용자 정보 조회. Apple은 ES256(client_secret JWT) + RS256(id_token 검증) |
| `payment/toss` | Toss 결제 API — 결제 승인/취소. domain 포트 `payment/domain/port/PgPaymentGateway` 구현(반환 타입은 포트의 `port/dto/PgConfirmResult`·`PgCancelResult`·`TossPaymentDetail`). `payment/toss/dto/` 내 provider 요청/응답 DTO |
| `mail/{javamail,ses}` | 메일 발송 — JavaMail SMTP(`JavaMailAdapter`) 또는 AWS SES(`AwsSesMailSender`). `mail/domain/port/MailSender` 구현. 설정은 `MailProperties`(접두어 `mail`) |
| `sms/{sns,solapi}` | SMS 발송 — AWS SNS 또는 Solapi. `sms/solapi/{request,response}` 패키지로 DTO 관리. `sms/domain/port/SmsSender` 구현 |
| `file/{s3,firebase}` | 파일 스토리지 — AWS S3 또는 Firebase Storage. domain 포트 `file/domain/port/FileStoragePort` 구현, `FileStorageStrategy` 패턴 |
| `crawling/bbq` | BBQ 치킨 가게 정보 크롤링 — `crawling/bbq/dto/` 내 응답 DTO 보유 |
| `exception/` | 어댑터 전용 예외 — `ExternalApiException`, `ExternalApiErrorCode` |

## For AI Agents

### Working In This Directory
- **Provider 격리**: 각 제공자별 독립 패키지. 예: `oauth/kakao/`는 카카오 로직만, `oauth/apple/`은 Apple 로직만 포함.
- **비밀키 관리**: 환경 변수(`.env`), JSON 파일(`json/`), 또는 설정에서만 주입. 코드에 하드코딩 금지.
- **Port 구현**: `MailSender`(`mail/domain/port/`), `SmsSender`(`sms/domain/port/`) 등 `domain-module`의 `<ctx>/domain/port/` 포트를 구현하되, 외부 응답 DTO를 도메인 모델로 직접 반환하지 않음. 포트는 프레임워크-프리이므로 WebClient/SDK 타입이 시그니처로 새어나가지 않게 한다.
- **DTO 격리**: 외부 API 응답은 `dto/`, `request/`, `response/` 패키지로 분리하여 domain 계층과 경계 명확화.

### Testing Requirements
- **외부 호출 모킹**: 단위 테스트에서 실제 외부 API 호출 금지. `WebClient` 등을 모킹.
- **통합 테스트 격리**: `bbq` 크롤링 같은 실제 네트워크 호출은 별도 테스트 클래스로 격리(`@Disabled` 또는 환경 조건 적용).
- **인증 정보 주입**: 테스트용 환경 변수 또는 프로파일 사용. 공개된 테스트 자격증명만 사용.

### Common Patterns
- **Provider DTO 폴더**: `payment/toss/dto/`, `sms/solapi/request/`, `sms/solapi/response/` 같이 provider별 DTO 그룹화.
- **WebClient 사용**: HTTP 호출은 `WebClient` 기반. 동기 호출은 `.block()`, 비동기는 `Mono/Flux` 체이닝.
- **Apple 로그인 JWT**:
  - `client_secret` 생성: JJWT `ES256` 서명 (비공개키 PKCS8 포맷, Base64 인코딩 저장)
  - `id_token` 검증: JJWT `RS256` 검증 (Apple JWKS에서 공개키 조회 후 RSA 검증)
- **조건부 Component**: `@ConditionalOnProperty`로 provider 선택 (예: `mail.provider=javamail|ses`, `sms.provider=sns|solapi`).

## Dependencies

### Internal
- `domain-module` — `<ctx>/domain/port/`의 출력 포트 인터페이스 및 도메인 타입(`com.tastyhouse.domain.*`)

### External
- **AWS SDK**: `software.amazon.awssdk:ses`, `software.amazon.awssdk:sns`, `io.awspring.cloud:spring-cloud-aws-s3`
- **Firebase**: `com.google.firebase:firebase-admin:9.10.0`
- **Spring Boot Starters**: `spring-boot-starter-mail`, `spring-boot-starter-web`, `spring-boot-starter-webflux`
- **JWT**: `io.jsonwebtoken:jjwt-api:0.12.3`, `jjwt-impl`, `jjwt-jackson` (Apple 로그인용 ES256/RS256)
- **Utilities**: `org.projectlombok:lombok`

<!-- MANUAL: -->

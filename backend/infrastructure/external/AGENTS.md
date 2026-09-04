<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-07-31 -->

# external-api

## Purpose
외부 시스템 연동 어댑터 라이브러리 모듈(`java-library`). 소셜 로그인(OAuth), 결제(Toss), 이메일(JavaMail/AWS SES), SMS(AWS SNS/Solapi), 파일 스토리지(AWS S3/Firebase), 가게 정보 크롤링을 캡슐화한다. `domain-module`이 선언한 출력 포트(`<ctx>/port/` — `mail/`의 `MailSender`, `sms/`의 `SmsSender`, `FileStoragePort`·`PgPaymentGateway`·`ProductReviewStatisticsPort`·`MemberReviewCountPort`)를 구현하는 어댑터 역할을 한다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + `domain-module`(implementation) + web/webflux, mail, AWS SES/SNS/S3, Firebase Admin, JJWT(Apple 로그인용). `bootJar` 비활성 |
| `src/main/resources/config/` | 외부 연동 설정 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/tastyhouse/external/` | 연동 어댑터 루트 (see `src/main/java/com/tastyhouse/external/AGENTS.md`) |
| `src/test/` | 연동 테스트 (`bbq` 크롤링 등) |

## For AI Agents

### Working In This Directory
- 각 연동은 제공자(provider)별 하위 패키지로 분리 (`oauth/kakao`, `payment/toss`, `sms/solapi` …).
- 외부 비밀키/자격증명은 코드에 하드코딩하지 말고 환경 변수(`.env`)·`json/`·설정에서 주입.
- `domain-module`의 출력 포트(`<ctx>/port/`)를 구현하되, 도메인 모델을 외부 응답 DTO로 오염시키지 않는다. 포트는 프레임워크-프리이므로 어댑터 쪽 DTO·WebClient 타입이 포트 시그니처로 새어나가지 않게 한다.
- **외부 응답 DTO는 도메인 타입을 보유하지 않는다 (역방향 누수 금지)**: 과거 `oauth/kakao/KakaoUserInfoResponse`·`oauth/naver/NaverUserInfoResponse`가 편의 매퍼에서 도메인 enum `MemberGender`를 직접 반환해 external-api → domain-module 역결합이 있었다(소비 측은 곧바로 `.name()`으로 되돌리고 있어 결합이 아무 값도 사지 못했다). 지금은 상수명 문자열(`"MALE"`/`"FEMALE"`/`null`)을 반환하며, 도메인 enum 승격은 소비 측이 `MemberGender.from(String)`으로 수행한다.
- **소셜 로그인은 이 모듈이 SPI를 소유한다 (`oauth/spi/`)**: `SocialOAuthClient`(`provider()`/`exchange()`/`fetchProfile()`)와 중립 값 타입(`SocialProfile`·`SocialCredential`·`SocialAuthorization`·`SocialProvider`). 제공자별 클라이언트 4종이 이를 구현하고, web-api는 **SPI만** 의존한다(제공자 패키지 직접 import는 web-api의 ArchUnit 규칙이 금지). 이 SPI를 domain-module에 두지 않은 이유는 소셜 OAuth의 호출부가 전부 표현 계층이라 도메인 서비스가 호출하는 포트가 아니기 때문이다 — 도메인 포트가 없는 공유 기술은 그 관심사를 쓰는 모듈이 소유한다는 모듈 경계 규칙(`security-module` 선례)을 따른다.
- **제공자별 관심사는 어댑터가 갖는다**: 페이스북 app_id 검증(`${facebook.app-id}` + `debug_token`)과 애플 id_token 검증 예외 번역(`APPLE_ID_TOKEN_INVALID`)은 과거 web-api 서비스에 있었으나 `exchange()`/`fetchProfile()` 안으로 회수했다. 응답 계약(`SOCIAL_OAUTH_FAILED`·`APPLE_ID_TOKEN_INVALID`)은 무변경이다. 애플 `fetchProfile`은 호출마다 JWKS를 네트워크로 받아 서명을 재검증하므로 값싼 조회가 아니다.

### Testing Requirements
- 외부 호출은 가능하면 모킹. 실제 네트워크 테스트는 `src/test/.../bbq` 처럼 격리.

### Common Patterns
- provider별 `dto/`로 요청/응답 매핑 (`payment/toss/dto`, `sms/solapi/request|response`).
- Apple 로그인은 client_secret JWT(ES256) 생성 + id_token(RS256) 검증 → JJWT 사용.

## Dependencies

### Internal
- `domain-module` (implementation) — 출력 포트 인터페이스(`<ctx>/port/`) 및 도메인 타입
- `application` (implementation) — **의존 역전(챕터 04)**. 이 모듈은 driven adapter이므로 자신이 구현하는 아웃바운드 포트를 소유한 모듈에 의존한다(방향: adapter → port). web 앱의 소셜 로그인 SPI(`auth.port.out`), batch 앱의 BBQ 메뉴·원격 이미지·행정동 경계 포트와 그 계약 타입이 그것이다. **반대 방향(`application → external-api`)은 `application/build.gradle`에서 제거됐으므로 순환이 아니다** — 그 줄을 되살리면 빌드가 깨진다

### External
- AWS SDK (SES, SNS), spring-cloud-aws-s3, Firebase Admin 9.10.0
- spring-boot-starter-mail, webflux(WebClient), JJWT 0.13.0 (Apple 로그인 — client_secret JWT 생성 ES256 + id_token 검증 RS256)
- **`spring-web`만 선언하고 `starter-web`은 쓰지 않는다** — 서블릿 스택 실사용이 `MultipartFile` 1종뿐이라 tomcat+webmvc 전체를 들일 이유가 없다. Jackson은 `starter-webflux`가 전이로 제공하므로 별도 선언이 없다

<!-- MANUAL: -->

<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-02 | Updated: 2026-06-02 -->

# external-api

## Purpose
외부 시스템 연동 어댑터 라이브러리 모듈(`java-library`). 소셜 로그인(OAuth), 결제(Toss), 이메일(JavaMail/AWS SES), SMS(AWS SNS/Solapi), 파일 스토리지(AWS S3/Firebase), 가게 정보 크롤링을 캡슐화한다. `core-module`의 port 인터페이스(예: `MailSender`, `SmsSender`)를 구현하는 infrastructure 어댑터 역할을 한다.

## Key Files
| File | Description |
|------|-------------|
| `build.gradle` | `java-library` + web/webflux, mail, AWS SES/SNS/S3, Firebase Admin, JJWT(Apple 로그인용). `bootJar` 비활성 |
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
- `core-module`의 port 인터페이스를 구현하되, `core` 도메인 모델을 외부 응답 DTO로 오염시키지 않는다.

### Testing Requirements
- 외부 호출은 가능하면 모킹. 실제 네트워크 테스트는 `src/test/.../bbq` 처럼 격리.

### Common Patterns
- provider별 `dto/`로 요청/응답 매핑 (`payment/toss/dto`, `sms/solapi/request|response`).
- Apple 로그인은 client_secret JWT(ES256) 생성 + id_token(RS256) 검증 → JJWT 사용.

## Dependencies

### Internal
- `core-module` — port 인터페이스 및 도메인 타입

### External
- AWS SDK (SES, SNS), spring-cloud-aws-s3, Firebase Admin 9.2.0
- spring-boot-starter-mail, webflux(WebClient), JJWT 0.12.3

<!-- MANUAL: -->

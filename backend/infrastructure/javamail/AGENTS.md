<!-- Parent: ../../AGENTS.md -->

# infrastructure:javamail

JavaMail(SMTP) 메일 발송 **벤더 모듈**(`java-library`). 도메인 포트 `MailSender`를 `JavaMailAdapter`가 구현한다. 메일 채널의 기본 벤더이며, 앱이 아니라 채널 모듈 `infrastructure:mail`이 `runtimeOnly`로 조립한다. AWS 대안은 `infrastructure:aws-ses`다.

`infrastructure:messaging` 4분할(2026-09-26)로 신설됐다. 패키지는 `external.mail.javamail` → `external.javamail`로 옮겼다 — 채널 모듈의 `@ComponentScan("com.tastyhouse.external.mail")`에 동반 스캔되지 않게 하기 위함이다.

## 무엇을 소유하는가

```
com.tastyhouse.external.javamail/
├── JavaMailModuleAutoConfiguration.java  @AutoConfiguration + @ComponentScan(이 패키지)
└── JavaMailAdapter.java                  MailSender 구현 @ConditionalOnProperty(mail.provider=javamail, matchIfMissing=true)
```

`application-javamail.yml`은 SMTP 접속 정보 `spring.mail.*`(호스트·포트·계정·starttls)를 담고, 채널의 `application-mail.yml`이 중첩 import로 로딩한다. 자격증명은 `${GMAIL_USERNAME}`·`${GMAIL_APP_PASSWORD}` 환경변수 참조다.

## 어느 앱이 의존하는가

앱은 이 모듈을 직접 의존하지 않는다. web-api가 `infrastructure:mail`을 `runtimeOnly`로 받고, 그 모듈이 이 모듈을 `runtimeOnly`로 조립해 web-api `runtimeClasspath`에 전이로 실린다. 벤더 전환은 이 모듈이 아니라 채널 모듈에서 한다(`../mail/AGENTS.md`).

## Dependencies

- `domain` (implementation) — `MailSender` 포트 + `BusinessException`·`ErrorCode.MAIL_SEND_FAILED`
- `spring-boot-starter-mail` — `JavaMailSender`. **이 좌표를 클래스패스에 올리는 유일한 모듈이다.**
- `infrastructure:mail`을 의존하지 않는다 — 발신자 주소는 `@Value("${mail.sender-address}")`로 키만 읽는다(순환 방지, `../mail/AGENTS.md` 봉인 목록)

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- `mail.provider` 조건은 구현 클래스에 붙어 있다. 모듈이 클래스패스에 있어도 provider가 다르면 빈이 등록되지 않는다. 기동 성공이 이 벤더가 선택됐다는 증거가 아니다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external.javamail` 봉인

**대상**: `backend/infrastructure/javamail/src/main/java/com/tastyhouse/external/javamail/`

`external.mail.javamail`로 되돌리면 채널 모듈 `infrastructure:mail`의 스캔에 동반 스캔된다. `com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### `JavaMailAdapter`가 `JavaMailMailSender`가 아닌 이유

**대상**: `backend/infrastructure/javamail/src/main/java/com/tastyhouse/external/javamail/JavaMailAdapter.java`

클래스명이 `JavaMailMailSender`가 아닌 것은 이 어댑터가 **주입받는 Spring의 `JavaMailSender`와 타입명이 혼동되기 때문**이며, `Adapter` 접미어로 구분한다. 포트 구현체 이름을 포트명에 맞춰 정리하려는 시도가 이 지점에서 되돌아오기 쉽다.

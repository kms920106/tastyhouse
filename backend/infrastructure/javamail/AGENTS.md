<!-- Parent: ../../AGENTS.md -->

# infrastructure:javamail

JavaMail(SMTP) 메일 발송 **벤더 모듈**(`java-library`). 포트 `MailSender`(`web-application`의 `com.tastyhouse.application.mail.port.out` — 과거 domain 소유, 02-vendor-ports로 `application`, 앱 마커 제거로 `web-application`)를 `JavaMailAdapter`가 구현한다. 메일 채널의 기본 벤더이며, 앱이 아니라 채널 모듈 `infrastructure:mail`이 `runtimeOnly`로 조립한다. AWS 대안은 `infrastructure:aws-ses`다.

`infrastructure:messaging` 4분할(2026-09-26)로 신설됐다. 패키지는 `external.mail.javamail` → `external.javamail`로 옮겼다 — 채널 모듈의 `@ComponentScan("com.tastyhouse.external.mail")`에 동반 스캔되지 않게 하기 위함이다. 이후 infrastructure 패키지 루트 통일로 루트가 `com.tastyhouse.infrastructure.javamail`이 됐다.

## 무엇을 소유하는가

```
com.tastyhouse.infrastructure.javamail/
├── ~~JavaMailModuleAutoConfiguration.java  @AutoConfiguration + @ComponentScan(이 패키지)~~ (번복됨 — imports 제거로 삭제. 빈은 앱 ModuleScanConfig의 com.tastyhouse.infrastructure 스캔이 등록)
└── JavaMailAdapter.java                  MailSender 구현 @ConditionalOnProperty(mail.provider=javamail, matchIfMissing=true)
```

`application-javamail.yml`은 SMTP 접속 정보 `spring.mail.*`(호스트·포트·계정·starttls)를 담고, 채널의 `application-mail.yml`이 중첩 import로 로딩한다. 자격증명은 `${GMAIL_USERNAME}`·`${GMAIL_APP_PASSWORD}` 환경변수 참조다.

## 어느 앱이 의존하는가

앱은 이 모듈을 직접 의존하지 않는다. web-api가 `infrastructure:mail`을 `runtimeOnly`로 받고, 그 모듈이 이 모듈을 `runtimeOnly`로 조립해 web-api `runtimeClasspath`에 전이로 실린다. 벤더 전환은 이 모듈이 아니라 채널 모듈에서 한다(`../mail/AGENTS.md`).

## Dependencies

- `web-application` (implementation) — `MailSender`·`MailSendResult` 포트의 소유 모듈. **앱 마커 제거로 `:application` → `:web-application`** (메일 발송 포트가 web 전용이라 web 앱 모듈로 옮겨갔다). ~~`domain` (implementation) — `MailSender` 포트 + `BusinessException`·`ErrorCode.MAIL_SEND_FAILED`~~ (02-vendor-ports 이전 기록 — 지금 `build.gradle`의 프로젝트 의존은 `:web-application` 하나다)
- `spring-boot-starter-mail` — `JavaMailSender`. **이 좌표를 클래스패스에 올리는 유일한 모듈이다.**
- `infrastructure:mail`을 의존하지 않는다 — 발신자 주소는 `@Value("${mail.sender-address}")`로 키만 읽는다(순환 방지, `../mail/AGENTS.md` 봉인 목록)

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- `mail.provider` 조건은 구현 클래스에 붙어 있다. 모듈이 클래스패스에 있어도 provider가 다르면 빈이 등록되지 않는다. 기동 성공이 이 벤더가 선택됐다는 증거가 아니다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external.javamail` 봉인

**대상**: `backend/infrastructure/javamail/src/main/java/com/tastyhouse/infrastructure/javamail/`

`external.mail.javamail`로 되돌리면 채널 모듈 `infrastructure:mail`의 스캔에 동반 스캔된다. ~~`com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다.~~ **(번복됨 — infrastructure 패키지 루트 통일)** persistence가 자기 루트 `com.tastyhouse.infrastructure.persistence`만 스캔하게 되면서 이 제약이 사라졌다. 지금 이 모듈의 루트는 `com.tastyhouse.infrastructure.javamail`이고 main 클래스는 전부 그 아래에 있어야 한다 — `backend/infrastructure/javamail/src/test/java/com/tastyhouse/infrastructure/javamail/architecture/VendorLayerRulesTest.java` → `shouldResideInModuleRootPackage`이 강제한다.

### 최상위 클래스에 `public`을 붙이지 않는다 (package-private 적용)

**대상**: `backend/infrastructure/javamail/src/main/java/com/tastyhouse/infrastructure/javamail/` 의 모든 최상위 타입 · 가드 `backend/infrastructure/javamail/src/test/java/com/tastyhouse/infrastructure/javamail/architecture/VendorLayerRulesTest.java` → `topLevelClassesShouldNotBePublic`

이 모듈의 최상위 타입은 전부 package-private이다(허용 목록 없음). 앱이 이 모듈을 타입 이름으로 부르지 않고 `ModuleScanConfig`의 문자열 스캔으로만 조립하며, 소비자는 `application`이 소유한 포트로만 주입받기 때문이다. `public`을 붙이면 다른 모듈이 구현에 직접 결합할 수 있게 되므로 가드가 `build/classes/java/main`의 최상위 클래스를 검사해 빌드를 실패시킨다. 생성자·메서드의 `public`은 유지한다. 근거와 전체 범주는 `backend/CLAUDE.md`의 "접근 제어자 규칙 (내부 구현은 package-private)" 절.

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### `JavaMailAdapter`가 `JavaMailMailSender`가 아닌 이유

**대상**: `backend/infrastructure/javamail/src/main/java/com/tastyhouse/infrastructure/javamail/JavaMailAdapter.java`

클래스명이 `JavaMailMailSender`가 아닌 것은 이 어댑터가 **주입받는 Spring의 `JavaMailSender`와 타입명이 혼동되기 때문**이며, `Adapter` 접미어로 구분한다. 포트 구현체 이름을 포트명에 맞춰 정리하려는 시도가 이 지점에서 되돌아오기 쉽다.

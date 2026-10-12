# ADR-0049: 인증 컨텍스트 패키지를 phoneverification·emailverification으로 바꾸고 타입·식별자 어휘(mail/sms)는 유지한다

- 상태: 채택
- 결정 커밋: `456db1b13` (2026-10-09) — 커밋 메시지 3번째 줄 `refactor(verification)`
- 대체한 결정: —

## 맥락

- **컨텍스트 패키지**: `domain`·`application`·`infrastructure.jpa`·`webapi` 아래의 1단계 하위 패키지(`order`, `shop`, `member` 등)로, 하나의 업무 영역을 뜻합니다. 같은 이름이 계층마다 반복돼 한 업무의 코드를 계층별로 찾아갈 수 있습니다.

휴대폰번호 인증과 이메일 인증만 업무 이름이 아니라 **발송 수단(기술) 이름** `sms`·`mail`을 컨텍스트 이름으로 쓰고 있었습니다. 같은 이름이 `infrastructure:sms`·`infrastructure:mail`(발송 벤더를 조립하는 채널 모듈)에도 있어, 이름만으로 업무 패키지인지 기술 모듈인지 구분되지 않았습니다(`docs/tasks/verification-context-rename/backend.md`).

## 이전 규칙

### backend/CLAUDE.md — 채널 도메인 어휘 통일 규칙 (`mail`/`sms`)

이 규칙은 `864b13b37`(2026-08-01)에서 시작됐습니다. "발송 채널이 곧 바운디드 컨텍스트인 도메인은 **패키지명과** 자바 식별자를 같은 어휘(`mail`/`sms`)로 통일한다"였습니다. 과거 `verification` 컨텍스트 하나가 이메일 인증과 휴대폰 인증을 함께 담았지만, 두 관심사는 애그리거트·리포지토리·출력 포트·이벤트·테이블이 처음부터 완전히 분리돼 있었고 공유하던 것은 `VerificationCode` VO 하나뿐이었습니다. 그래서 둘을 나누면서 패키지와 타입 모두 채널 어휘를 쓰게 했습니다.

- 나눈 근거에는 "`external-api`가 이미 `email/`·`sms/`로 갈라져 있어 도메인 쪽만 통합 상태로 남은 불일치"도 있었습니다.
- 당시 reference 구현의 패키지 경로는 채널 어휘였습니다: `domain`의 `mail/domain/`(`MailVerification`·`MailVerificationPurpose`·`MailVerificationMessage`·`MailSender`)·`sms/domain/`(`SmsVerification`·`SmsVerificationMessage`·`SmsSender`), `web-api`의 `mail/MailVerificationApiController`·`sms/SmsVerificationApiController`, 서비스는 `application/mail/service/MailVerificationService`·`application/sms/service/SmsVerificationService`, 발송 포트는 `web-application`의 `{mail,sms}.port.out`.
- 같은 규칙의 테이블 항목은 처음에는 "`ddl-auto=validate` 환경이라 RENAME은 DDL 마이그레이션과 앱 배포가 원자적이어야 하므로 순수 코드 리팩터에 그 운영 리스크를 들이지 않는다"는 이유로 `EMAIL_VERIFICATION`/`PHONE_VERIFICATION`을 유지했습니다. 이후 사용자 결정으로 `alter.sql`에 `RENAME TABLE`·`RENAME INDEX`를 추가해 `MAIL_VERIFICATION`/`SMS_VERIFICATION`(인덱스 `idx_mail_verification_*`/`idx_sms_verification_*`)으로 바꿨습니다. 이 테이블·인덱스명은 이번 결정에서도 불변입니다.

### backend/CLAUDE.md — infrastructure 패키지 규칙

jpa의 인증 컨텍스트 패키지는 `com.tastyhouse.infrastructure.jpa.{mail,sms}`였습니다.

## 결정

컨텍스트 패키지는 업무 이름을 쓰고, 기술 이름(`sms`·`mail`·`oauth`·`pg`)은 infrastructure 모듈명에만 남깁니다. 클래스명·테이블·URL·에러코드 등 **타입·식별자 어휘는 그대로** `Mail*`/`Sms*`입니다.

| 계층(모듈) | before | after |
|---|---|---|
| domain | `com.tastyhouse.domain.sms` / `.mail` | `com.tastyhouse.domain.phoneverification` / `.emailverification` |
| application(코어)·web-application | `com.tastyhouse.application.sms` / `.mail` | `com.tastyhouse.application.phoneverification` / `.emailverification` |
| infrastructure:jpa | `com.tastyhouse.infrastructure.jpa.sms` / `.mail` | `com.tastyhouse.infrastructure.jpa.phoneverification` / `.emailverification` |
| web-api | `com.tastyhouse.webapi.sms` / `.mail` | `com.tastyhouse.webapi.phoneverification` / `.emailverification` |
| 도메인 문서 | `docs/domain/sms.md` / `mail.md` | `docs/domain/phoneverification.md` / `emailverification.md` |
| 클래스명·테이블·URL·에러코드·Redis 키·infra 모듈명 | `SmsVerification`·`MAIL_VERIFICATION`·`/api/sms-verifications` 등 | 불변 |

## 근거

- 패키지 이름이 기술 모듈명과 겹치면, 그 패키지가 업무 영역인지 기술 어댑터인지 이름만으로 알 수 없습니다.
- 타입·식별자 어휘까지 바꾸면 테이블 RENAME·URL·에러코드 문자열 같은 wire 계약과 운영 리스크가 따라옵니다. 패키지만 옮기면 import 치환으로 끝나는 리팩터링입니다.

## 결과·감수한 손실

- 동작은 바뀌지 않았습니다. 디렉터리 18개, java 파일 60개를 옮겼고 `schema.sql`·DDL 변경은 없습니다.
- 채널 어휘 통일 규칙 중 **패키지 어휘에 관한 부분만** 바뀌었고, 타입·식별자 어휘 규칙(`Mail*`/`Sms*`, 값 이름은 `email`/`phoneNumber`, `@RateLimit keyPrefix` 개명 금지 등)은 유효합니다.
- 문자열 FQN을 쓰는 `ServiceContextBoundaryTest.SEALED_VIOLATIONS`의 `MailVerificationService` 항목도 새 패키지로 바뀌었습니다.
- jpa의 인증 컨텍스트가 `jpa.{emailverification,phoneverification}`이 되어, 예약된 스타터 이름(`mail`·`sms`)과 애초에 충돌하지 않습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 채널 도메인 어휘 통일 규칙 (`mail`/`sms` — 타입·식별자 어휘. 패키지는 업무 이름) · 컨텍스트 패키지는 업무 이름, 기술 이름은 infrastructure 모듈명에만
- `backend/CLAUDE.md` · infrastructure 패키지 규칙 (루트 = 모듈명) · jpa 인증 컨텍스트 `jpa.{emailverification,phoneverification}`
- `backend/CLAUDE.md` · 인증코드 발송은 발급과 원자적으로 수행하는 규칙 · reference 경로 `application/emailverification`·`application/phoneverification`
- `docs/domain/phoneverification.md`, `docs/domain/emailverification.md` · 휴대폰번호 인증(phoneverification) 도메인 / 이메일 인증(emailverification) 도메인 · 도메인 문서 이름이 컨텍스트 패키지 이름(업무 이름)과 같다

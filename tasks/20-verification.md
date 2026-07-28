# verification 도메인 전환

> 선행: `tasks/README.md` + `00-phase0` + 그룹 1 완료. 그룹 2(병렬 가능). **출력 포트·이벤트 리스너 보유.**

## 현황
- core: `verification/application/` — `EmailVerificationCommandService`·`PhoneVerificationCommandService`(각 1 repo, 이벤트 발행), `event/VerificationEventListener`, **출력 포트 `application/port/out/MailSender`·`SmsSender`**(external-api가 구현).
- 소비자: web-api(인증코드 발송·검증, `AuthPasswordResetService`가 도메인 모델 직접 사용).

## 작업
1. **포트 이동**: `application/port/out/MailSender`·`SmsSender` → `domain/verification/port/`(패키지 `core.domain.verification.domain.port`). external-api 구현체의 import 갱신.
2. **(E) 리스너**: `VerificationEventListener` → infrastructure-module `verification/listener/`로 이동. 본문이 메일/SMS 발송 포트 호출이면 그대로 포트 주입 유지.
3. **(C) 판정**: 코드 발송이 "Verification save + 이벤트 발행(→발송)"이면 도메인 서비스 `VerificationIssueService`(가칭)로 하강, 발행은 `DomainEventPublisher` 포트 사용. `confirmVerificationCode`(검증+상태전이+save)도 web·향후 ceo가 공유할 규칙이므로 도메인 서비스로 하강 권장.
4. **(A)**: web `AuthPasswordResetService` 등 파사드는 `@Transactional` 부여 후 도메인 서비스 호출로 교체(명시적 save 규칙 유지). read model이 생기면 infra `infrastructure/verification/query/`(패턴 3)로 — 단 인증코드 검증용 조회는 write 포트 잔류 기준(README) 적용.
5. core `verification/application/` 삭제.

## 완료 기준
- external-api 포함 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.

# ADR-0006: Request→Command 변환은 Request의 toCommand로 컨트롤러가 소유한다

- 상태: 채택
- 결정 커밋: `01d6d7494` (2026-08-29)
- 대체한 결정: —

## 맥락

- **Request**: HTTP 요청 본문을 받는 record(검증 + Swagger 스키마).
- **Command**: application 계층 유스케이스가 받는 입력 record.

이전에는 Request를 순수 데이터 홀더로 두고, 컨트롤러가 필드를 하나씩 꺼내 Service 메서드 인자로 넘겼다. 완전 매핑 전략([ADR-0005](0005-inbound-port-full-mapping.md))을 채택하면서 "계층 경계의 매핑은 누가 하는가"를 다시 정했다. 같은 커밋에서 0005 다음으로 들어간 결정이다.

## 이전 규칙

### `backend/CLAUDE.md` — DTO 조립 규칙 (`d60628c43`, 2026-07-05)

원시 파라미터 → command/VO/condition 변환은 대상 record 자신의 정적 팩토리 `of(...)`로 한다. **Request DTO는 command 생성 책임을 지지 않는 순수 데이터 홀더(검증 + Swagger 스키마)로 유지하고, `toCommand()` 같은 변환 메서드를 두지 않는다.** 컨트롤러가 개별 원시 필드로 언패킹해 Service에 전달한다.

## 결정

| 항목 | before | after |
|---|---|---|
| 변환 메서드 | Request에 `toCommand()` 금지 | Request에 `toCommand(...)`를 둔다 |
| 변환 주체 | 컨트롤러가 원시 필드를 낱개로 언패킹 → Service 다인자 호출 | 컨트롤러가 `XxxCommand command = request.toCommand(경로변수, principal 식별자, ...);`로 Command 조립 |
| 본문에 없는 값(경로 변수·인증 주체) | Service 인자로 직접 | `toCommand`의 파라미터로 주입(`request.toCommand(id, userDetails.getMemberId())`) |

## 근거

- 계층 경계의 매핑은 그 경계를 지키는 어댑터의 책임이다(완전 매핑 전략).
- 낱개 언패킹 방식은 필드가 늘수록 Service 시그니처가 길어져 **파라미터 15개짜리 메서드**를 낳았다.
- 같은 타입(Long/String) 인자가 줄줄이 늘어선 호출부에서 **순서를 착각해 값이 조용히 뒤바뀌는 사고**(컴파일은 통과)가 반복됐다. 이름 있는 record 필드로 묶으면 그 사고 유형 자체가 사라진다.

## 결과·감수한 손실

- wire 계약(요청/응답 JSON·에러 코드·HTTP 상태)은 바뀌지 않았다(커밋 메시지).
- Request가 application Command 타입을 알게 됐다. Request는 여전히 domain-free·infra-free이며, 문자열 → 도메인 enum 승격은 Request에서 하지 않는다.
- `toCommand` 안에서도 같은 타입 인자의 위치 기반 조립은 위험하므로 일부 Request에는 "위치 기반 조립 금지" 봉인이 붙어 있다(예: ceo-api `ProductNutritionUpdateRequest.toCommand`).

## 영향받은 문서

- `backend/CLAUDE.md` · "DTO 조립 규칙 (`new` 직접 호출 지양)" · Request → Command 변환은 컨트롤러가 `toCommand`로 소유
- `backend/AGENTS.md` · "Working In This Directory" · DTO 조립은 `new` 직접 호출 지양, Request는 `toCommand`
- `backend/ceo-api/AGENTS.md` · "봉인·가드 목록" > "`ProductNutritionUpdateRequest.toCommand` — 위치 기반 조립 금지" · `toCommand` 작성 시 주의

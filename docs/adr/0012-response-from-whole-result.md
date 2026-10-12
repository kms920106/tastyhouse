# ADR-0012: Response.from은 읽기 계약 XxxResult를 통째로 받는다

- 상태: 채택
- 결정 커밋: `787dd580f` (2026-09-01)
- 대체한 결정: —

## 맥락

- **Response record**: HTTP 응답 본문 record. 정적 팩토리 `from(...)`으로 조립한다.
- **읽기 계약 `XxxResult`**: 조회 결과 record. 지금은 `com.tastyhouse.application..port.out`에 있는 프레임워크-프리 계약이다.

챕터 06(Response 계약 승격)이 Response record를 application 모듈에서 api 모듈로 옮기고 api 모듈의 Service를 삭제했다. 그러자 "Result를 Response로 바꾸는 언패킹은 Service가 한다"는 기존 규칙이 지킬 수 없는 문장이 됐다.

## 이전 규칙

### `backend/CLAUDE.md` — DTO 조립 규칙 — "도메인/DTO → 응답 변환 (result 객체가 아니라 개별 원시타입으로 수신)"

응답 record의 정적 팩토리 `from(...)`은 infra query DAO의 result 객체(`XxxResult`)를 통째로 받지 않고, **result의 각 필드를 원시타입(String/Long/Integer/LocalDateTime 등)으로 낱개 언패킹해서 받는다**(`XxxResponse.from(id, title, content, ...)`). result를 낱개로 풀어 넘기는 책임은 이 Response를 호출하는 **Service**가 지며, Service에 private 매퍼(`toXxxResponse(XxxResult dto)`)를 두어 `dto.id()`, `dto.title()`처럼 이름 기반으로 꺼낸다. 근거는 "result 객체를 그대로 받으면 Response record가 infra query result의 필드 구조를 알아야 해 infrastructure에 결합된다"였다.

## 결정

| 항목 | before | after |
|---|---|---|
| `from` 시그니처 | `from(Long id, String title, ...)` 원시타입 낱개 | `from(XxxResult result)` 통째로 |
| 매퍼 위치 | Service의 private `toXxxResponse` | Response record 자신. 중첩 조립(리스트 필드·하위 Response)도 그 record의 private 헬퍼 |

## 근거

1. **문언이 지정한 언패킹 주체가 사라졌다** — 규칙은 그 책임을 "Service"에 맡기는데, 챕터 06이 api 모듈의 Service를 삭제했고 `apiModuleMustNotContainApplicationLayer`가 재생성을 막는다. 남는 후보는 컨트롤러뿐이라, 규칙을 지켜도 언패킹이 api 모듈에 그대로 남는다(위반이 이동만 한다).
2. **`PageResult.map`이 arity 1을 요구한다** — `PaginationResponse.from(pageResult.map(XxxResponse::from))` 형태가 admin-api에 56곳이고 `PageResult#map`은 `Function<T,R>`이다. 다인자 `from(Long, String, ...)`은 메서드 레퍼런스가 될 수 없어, 문언을 지키려면 56곳을 인라인 람다로 풀어야 하고 **그러면 같은 타입 인자의 위치 착오 위험이 응답 타입 85개에 되살아난다.**
3. **폐기된 근거** — "infrastructure에 결합된다"는 더 이상 성립하지 않는다. `from`이 받는 타입은 `com.tastyhouse.infrastructure..`의 result가 아니라 `com.tastyhouse.application..port.out`의 읽기 계약이고(챕터 04 읽기 경로 포트화·챕터 05 소유 규칙의 결과), 그 계약은 프레임워크-프리라 api 모듈이 정당하게 아는 대상이다. 결합 대상이 "infra 구현"에서 "계약"으로 바뀌었다.

## 결과·감수한 손실

- Response가 Result의 필드 구조를 안다. Result 필드가 바뀌면 그것을 쓰는 Response도 함께 바뀐다 — 계약 결합이므로 감수한다.
- 이후 ceo(챕터 09)·web(챕터 10)도 같은 형태로 Response를 api 모듈로 승격해 3개 앱 전부 `from(Result)`를 쓴다.

## 영향받은 문서

- `backend/CLAUDE.md` · "DTO 조립 규칙 (`new` 직접 호출 지양)" · 응답 `from(...)`은 읽기 계약 `XxxResult`를 통째로 받는다
- `backend/CLAUDE.md` · "Response record 정적 팩토리 시그니처 줄바꿈 규칙" · `from` 시그니처 형태
- `backend/AGENTS.md` · "Working In This Directory" · 응답 조립의 `from(...)`은 `XxxResult`를 통째로 받는다
- `backend/application-core/AGENTS.md` · "`response/`는 각 api 모듈로 승격됐다 (챕터 06 · 09 · 10)" · Response 조립 주체는 api 모듈

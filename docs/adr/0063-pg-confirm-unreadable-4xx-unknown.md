# ADR-0063: 본문을 해석할 수 없는 PG 승인 4xx는 거절이 아니라 결과 불명으로 분류한다

- 상태: 채택
- 결정 커밋: 커밋 후 기입
- 대체한 결정: ADR-0060(결과 불명을 5xx·통신 예외·빈 응답으로 한정하던 부분)

## 맥락

[ADR-0060](0060-pg-confirm-unknown-lookup.md)은 결과를 알 수 없는 승인 응답을 `PgConfirmFailure.UNKNOWN`으로 분류했습니다. UNKNOWN이면 결제를 FAILED로 확정하지 않고, 토스에 조회해 대사합니다. 그런데 UNKNOWN의 범위는 5xx·통신 예외·빈 응답 세 가지뿐이었습니다.

리뷰에서 다음 경로가 지적됐습니다. 승인 응답이 4xx인데 본문을 토스 에러 JSON으로 읽지 못하면 `TossPaymentClient#translateResponseError`가 `PG_API_ERROR`를 붙입니다. `TossPaymentGatewayAdapter#toFailure`는 이 코드를 `REJECTED`로 번역합니다. 그 결과 결제는 조회 없이 FAILED가 됩니다.

토스페이먼츠 문서(API 에러 코드 · 인증 및 기타 헤더 설정)로 다음을 확인했습니다.

- 토스는 실패할 때 HTTP 상태 코드와 함께 항상 `{code, message}` JSON 에러 객체를 돌려줍니다.
- 에러 코드 표에 408·429가 없습니다.

따라서 HTML 본문이나 code가 빈 4xx는 토스의 정상 에러 응답이 아닙니다. 중간 인프라(프록시·로드밸런서·CDN)가 만든 응답일 수도 있고, 토스가 직접 보낸 비JSON 응답일 수도 있습니다. 토스 문서는 라이브 환경에서 요청이 과다하면 `429 Too Many Requests`가 올 수 있다고 하면서(거래 조회) 그 본문 형식은 적지 않았습니다. 테스트 환경 실측에서는 요청 제한을 넘긴 429가 JSON(`TOO_MANY_REQUESTS`)이었으므로 그 429는 이 결정의 대상이 아니라 기존대로 `REJECTED`입니다. 본문이 JSON이 아니거나 code가 빈 응답은 출처를 알 수 없으므로 토스가 승인을 처리했는지 알 수 없습니다. 그런데도 거절로 확정하면, 돈은 나갔는데 결제가 FAILED인 상태가 생길 수 있습니다. 이것은 ADR-0060이 막으려던 상태와 같습니다.

## 이전 규칙

- 4xx 본문이 JSON이 아니거나 깨졌거나 code가 비어 있으면 `PG_API_ERROR`를 붙였습니다. 근거는 "토스가 4xx로 거절했다는 사실은 확실하므로 결과 불명이 아니다"였습니다.
- 결과 불명 코드는 `PG_SERVER_ERROR`·`SYSTEM_ERROR`·`UNKNOWN_ERROR` 3개였습니다.

## 결정

| 항목 | before | after |
|---|---|---|
| 승인 4xx + 해석할 수 없는 본문(비JSON·깨진 JSON·빈 code) | `PG_API_ERROR` → `REJECTED` → FAILED 확정 | `PG_UNREADABLE_RESPONSE` → `UNKNOWN` → 즉시 토스 조회로 대사 |
| 승인 4xx + 토스 JSON(code 있음) | 토스 code 보존 → `REJECTED`/`ALREADY_PROCESSED` | 변경 없음 |
| 조회 4xx + 해석할 수 없는 본문 | `PG_API_ERROR` → 조회 `UNKNOWN` | 코드만 `PG_UNREADABLE_RESPONSE`로 바뀌고 결과(`UNKNOWN`)는 같음 |
| `PG_API_ERROR` 사용처 | 승인 폴백 + 취소 | 취소 전용 |

## 근거

- 결과를 모르는 응답을 거절로 확정하면 되돌릴 수 없는 불일치(돈은 나갔는데 결제는 FAILED)가 생깁니다. 반대로 진짜 거절을 UNKNOWN으로 분류하면 조회가 `NOT_APPROVED`를 돌려줘 같은 실패에 도달합니다. 이때 손해는 조회 1회만큼의 지연뿐입니다. 비용이 비대칭이므로 안전한 쪽을 택합니다.
- 기존 UNKNOWN 흐름(즉시 조회, 503 `PAYMENT_APPROVAL_UNCONFIRMED`, batch 주기 대사)을 그대로 재사용하므로 응답 계약은 바뀌지 않습니다.

### 고려한 대안

- **감수(현행 유지)**: 변경이 없습니다. 하지만 출처를 알 수 없는 4xx를 받는 드문 경우에 돈과 결제 상태가 어긋날 수 있습니다.
- **408·429만 UNKNOWN**: 의도는 명확합니다. 하지만 상태 코드만으로는 토스 응답인지 판별할 수 없습니다. 프록시가 만든 403·400 같은 다른 비JSON 4xx는 여전히 FAILED로 확정됩니다.
- **해석할 수 없는 4xx 전체를 UNKNOWN (채택)**: 판별 기준이 "토스 형식의 응답인가"라는 하나로 정리됩니다.

### 경계

토스가 JSON code를 담은 4xx를 돌려주면 계속 `REJECTED`입니다. 가정상 429에 code가 있는 경우도 마찬가지입니다. 토스 에러 코드 문서에 408·429가 없으므로 이 경우는 범위에서 뺐습니다.

## 결과·감수한 손실

- 진짜 토스 거절인데 본문만 깨진 경우(가능성이 극히 낮습니다)에는 조회 1회만큼 응답이 늦어집니다. 최악의 경우 승인과 조회를 합쳐 약 30초가 걸리며, ADR-0060의 상한과 같습니다.
- 사용자 메시지가 바뀝니다. 조회가 `NOT_APPROVED`면 원문(`결제 승인하기 API 호출에 실패했습니다: 400 …`) 대신 기본 메시지 "결제 승인에 실패했습니다."가 나갑니다. HTTP 400과 에러코드 `PAYMENT_APPROVAL_FAILED`는 같습니다.
- 원장 `TOSS_PAYMENT_RECORD`에는 최초 승인 4xx가 아니라 조회 결과가 남습니다. 조회 응답의 `status`(예: `ABORTED`)와 토스 `failure.code`가 남고, 조회가 404면 `failureCode`가 `NOT_FOUND_PAYMENT`입니다. 승인 4xx 원문은 로그(클라이언트 WARN·어댑터 ERROR)에만 남습니다. 기존 5xx 경로와 같은 동작입니다.

## 영향받은 문서

- `backend/infrastructure/tosspayments/AGENTS.md` · 승인 4xx 응답은 토스 본문의 code·message를 보존한다 — 폴백을 결과 불명으로 변경, 결과 불명 코드 4개, `PG_API_ERROR` 취소 전용
- `backend/infrastructure/tosspayments/AGENTS.md` · 주문번호로 결제 조회 — 결과 불명 대사용 — 오류 변환 목록에 새 코드 추가
- `backend/application-core/AGENTS.md` · `PgConfirmFailure`는 세 가지다 / PG 호출 자체가 예외·5xx·빈 응답이면 결제는 PENDING — 해석 불가 4xx 추가
- `docs/domain/payment.md` · 결과 불명 정의 — "결제 대행사가 만든 것이 아닌, 해석할 수 없는 오류 응답" 추가

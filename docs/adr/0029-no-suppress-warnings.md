# ADR-0029: 경고를 @SuppressWarnings로 가리지 않는다(XxxCodes 복제 상수 삭제·SensitiveFieldMasker 배선)

- 상태: 채택
- 결정 커밋: `d555b0325` (2026-09-27)
- 대체한 결정: [ADR-0028](0028-persistence-state-layer.md)(일부 — `XxxCodes` 복제 상수와 그 일치 검사)

## 맥락

`@SuppressWarnings`는 IDE·컴파일러 경고(미사용·타입 불안전 등)를 끄는 어노테이션입니다. 경고를 끄면 경고가 가리키던 사실이 코드에서 보이지 않게 되고, 나중에 그 상태가 바뀌어도 아무도 알아채지 못합니다. 이 저장소에서 실제로 이런 일이 있었습니다.

- **`XxxCodes` 복제 상수**: 덩어리 03b([ADR-0028](0028-persistence-state-layer.md))에서 persistence가 도메인 enum을 모르게 하려고, `application/<ctx>/port/out/XxxCodes`에 도메인 enum을 문자열 상수로 복제한 클래스 13개를 두었습니다. DAO가 쓰지 않는 상수마다 미사용 경고가 나서 `@SuppressWarnings("unused")`가 붙었습니다. 복제본은 도메인 enum과 두 벌이라 일치 검사(`codesMatchDomainEnums`)도 따로 필요했습니다.
- **`SensitiveFieldMasker` 미배선**: 로그의 민감 필드(비밀번호·토큰)를 가리는 클래스가 어디에도 주입되지 않아 미사용 경고가 억제돼 있었습니다. 즉 마스킹이 실제로는 전혀 동작하지 않았고, `[BODY]` 로그에 민감 값이 그대로 찍혔습니다.
- `ProductScheduler`는 호출부 없는 스케줄러라 억제 2개가 붙어 있었고, 테스트에는 `unchecked`·`SameParameterValue` 억제가 있었습니다.

## 이전 규칙

### backend/application-core/AGENTS.md — 덩어리 03b의 enum 비교값 전달 규칙

persistence가 비교할 enum 상수는 `<ctx>/port/out/XxxCodes`(도메인 enum을 복제한 문자열 상수 클래스)에서 가져온다. DAO 리터럴 `"COMPLETED"`는 도메인 enum 상수가 개명·삭제돼도 컴파일되고 조회가 조용히 0건이 되므로, `XxxCodes` 복제본을 거친다. 스캔 기반 가드 `codesMatchDomainEnums`가 복제본과 도메인 enum의 일치를 검사한다.

### backend/CLAUDE.md — 도메인 enum 경계 규칙

DAO가 enum 상수와 비교할 때는 `application/<ctx>/port/out/XxxCodes` 문자열 상수를 쓴다(예: `.eq(OrderStatusCodes.COMPLETED)`).

### backend/logging-module/AGENTS.md — 규칙

`SensitiveFieldMasker`는 미배선 상태라 마스킹이 실제로는 동작하지 않는다. 마스킹이 실사용에 연결되지 않은 지금은 `SENSITIVE_FIELDS` 목록만 갱신해도 즉시 효과가 없다. 마스킹을 실제로 적용하려면 `ApiLoggingAspect` 활성화가 먼저 필요하다.

### backend/logging-module/AGENTS.md — 패키지 구조

`SensitiveFieldMasker`는 어디서도 주입·호출되지 않았고 클래스에 `@SuppressWarnings("unused")`가 붙어 있었다. `[BODY]` 로그는 record `toString()` 형식(`LoginRequest[username=a, password=b]`)으로 찍혔고, 마스킹 로직은 배열 원소 안의 객체로 재귀하지 않아 배열 원소의 민감 필드가 그대로 남았다.

### backend/infrastructure/jpa/AGENTS.md — getter 없는 필드를 "미사용"이라며 지우지 않는다

getter 없는 필드에 `@SuppressWarnings("unused")`가 붙어 있는 자리도 같은 이유로 남기며, 그 어노테이션을 떼지 않는다.

## 결정

**IDE·컴파일러 경고는 `@SuppressWarnings`로 가리지 않고 원인을 고칩니다.** 새 코드에는 붙이지 않고, 기존 코드를 고칠 때도 되살리지 않습니다. 억제 말고는 방법이 없을 때만 쓰고, 그 이유는 소유 모듈 `AGENTS.md` 봉인 항목에 적습니다(주석 금지 규칙 때문).

| 경고 | before (억제) | after (원인 해소) |
|---|---|---|
| 미사용 상수 (`unused`) | `XxxCodes` 복제본 13개에 억제 + `codesMatchDomainEnums` 일치 검사 | 복제본 13개 삭제. application이 도메인 enum `name()`을 포트 인자로 넘김. 쿼리 모양이 바뀌는 분기는 스펙 record(`ReviewSortSpec`·`ShopReviewTabFilter`·`ProductExposureWindow`), 고정값은 `ShopDeliveryTipRangePolicy` |
| 옵션 유형 리터럴 | DAO의 `"NORMAL"`/`"COMMON"` 리터럴 4곳 | `ProductAvailabilityQueryPort#findProductOptionAvailability(condition, normalOptionType, commonOptionType)` 인자 |
| 복제본 가드 | `EnumCodeConstantsTest`(일치 검사) | 복제본 금지 가드 2종(상수 집합·문자열 상수) — `portOutShouldNotMirrorDomainEnums` |
| 호출부 없는 스케줄러 | `ProductScheduler`에 억제 2개 | `@Scheduled(cron = "${product.option-crawl.cron:-}")`. 기본값 `-`는 비활성 cron이라 동작은 그대로 |
| 주입되지 않은 빈 | `SensitiveFieldMasker`에 억제(마스킹 미동작) | `ApiLoggingAspect`에 주입해 `[BODY]` 로그를 마스킹(**동작 변경**) |
| 제네릭 캡처 (`unchecked`) | `ArgumentCaptor.forClass(List.class)` + 억제 | `ArgumentCaptor.captor()`(Mockito 5.x) |
| 항상 같은 인자 (`SameParameterValue`) | 테스트 fake 헬퍼에 억제 | 파라미터를 없애고 헬퍼 이름에 의미를 담음(`registerInGangnam(dongName)`) |

## 근거

- 억제 어노테이션은 경고가 가리키던 사실(미사용·미배선·타입 불안전)을 지워 버립니다. 마스킹 미배선이 그 실례였습니다. 억제가 붙어 있는 동안 민감 필드가 로그에 그대로 찍혔습니다.
- `XxxCodes` 복제본은 도메인 enum과 두 벌이라 일치 검사가 따로 필요했습니다. 게다가 "판매 완료 주문만 센다"·"탈퇴 회원 제외" 같은 **도메인 정책이 persistence에 박혀** 있었습니다. 비교값을 application이 도메인 enum `name()`으로 넘기면 상수가 개명·삭제될 때 **application 호출부가 컴파일 에러**를 내므로, 리터럴의 "조용한 0건" 위험도 함께 막힙니다.

## 결과·감수한 손실

- persistence가 enum 어휘를 전혀 모르게 됐습니다. HTTP 응답과 저장값은 바뀌지 않았습니다(인기상품 기준 시각을 한 번만 읽도록 통일).
- `[BODY]` 로그 형식이 record `toString()`에서 마스킹된 JSON(`{"username":"a","password":"***"}`)으로 바뀌었습니다(동작 변경 — 로그 내용만 바뀌고 요청 처리는 불변). `ApiLoggingAspect#logControllerExecution`이 각 바디를 `sensitiveFieldMasker.mask(...)`로 변환하며, 배열 원소도 재귀해 마스킹합니다. 직렬화에 실패하면 요청은 막지 않고 바디 자리에 `[직렬화 실패]`를 남깁니다(로깅이 요청을 막지 않는다는 불변 유지). 이제 `SENSITIVE_FIELDS` 목록 갱신이 즉시 반영되며, 마스킹 규칙은 `SensitiveFieldMaskerTest`가 검사합니다.
- 함께 고친 결함이 있습니다. 저장 링이 비면 NPE 대신 빈 목록을 돌려줍니다.
- 규칙 도입 시점에 정리하지 못한 억제 9곳(main 4곳·테스트 5곳)이 남아 있습니다. 일부는 각 모듈 `AGENTS.md` 봉인 항목에 근거가 있습니다.
- 이후 [ADR-0031](0031-persistence-domain-reallowed.md)로 쓰기 어댑터(`XxxPersistenceAdapter`)는 도메인 enum을 다시 참조할 수 있게 됐습니다. 쓰기 어댑터는 `XxxStatus.X.name()`으로 비교값을 만들고(리터럴 금지는 유지), 조회 DAO는 여전히 포트 인자로 받습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · "`@SuppressWarnings` 지양 규칙" · 경고는 억제하지 않고 원인을 고치며, 도입 때 바꾼 곳의 before/after 표와 남은 9곳
- `backend/CLAUDE.md` · "도메인 enum 경계 규칙" · DAO 비교값은 포트 인자로 받고 복제 상수를 두지 않는다(`portOutShouldNotMirrorDomainEnums`)
- `backend/application-core/AGENTS.md` · "읽기 계약 평탄화 — `port/out`은 domain을 모른다" · enum 비교값 전달 규칙
- `backend/application-core/AGENTS.md` · "DAO에 enum 상수명을 리터럴로도, 복제 상수로도 쓰지 않는다 — 비교값은 포트 인자로 받는다" · 리터럴·복제 상수 금지와 잔존 검사
- `backend/infrastructure/jpa/AGENTS.md` · `getter 없는 필드를 "미사용"이라며 지우지 않는다` · 억제 어노테이션 없이 필드를 유지한다
- `backend/logging-module/AGENTS.md` · "패키지 구조" / "규칙" · `ApiLoggingAspect`가 `SensitiveFieldMasker`로 `[BODY]`를 마스킹하고, 목록 갱신이 즉시 반영된다

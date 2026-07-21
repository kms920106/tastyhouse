# 작업지시서 03 — 네이밍 일관성 및 컨벤션 위반 정리

## 배경 (왜)

전수 조사에서 web-api·admin-api 두 모듈에 걸쳐 네이밍·컨벤션 일관성이 깨진 지점 3가지 유형이 발견되었다. 이들은 각각 독립적으로 처리 가능하며, 공통점은 "코드베이스 대다수 패턴과 다르게 짜여진 소수 지점을 다수 패턴에 맞춰 정리"한다는 것이다.

## 현재 상태 (근거)

### 1. Facade/Service 네이밍 혼재

web-api는 39개 클래스가 `XxxService` 네이밍을 쓰는데, 다음 2개만 `Facade`를 쓴다:

- `web-api/src/main/java/com/tastyhouse/webapi/member/MemberFacade.java`
- `web-api/src/main/java/com/tastyhouse/webapi/auth/AuthFacade.java`

admin-api는 예외 없이 전부 `Service` 네이밍을 사용한다. 그런데 루트 `CLAUDE.md`와 각 모듈 `AGENTS.md`는 "Facade 패턴"을 프레젠테이션 계층의 표준 용어로 서술하고 있어(문서상 web-api/admin-api Facade 규칙 절 참고), **실제 코드의 다수파(Service)와 문서의 용어(Facade)가 불일치**하는 상태다.

### 2. 컨트롤러 core import 위반 (유일 1건)

`web-api/src/main/java/com/tastyhouse/webapi/search/SearchApiController.java:15-16`에서 다음을 import한다:

```java
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
```

이 컨트롤러는 `validateKeyword`라는 검증 헬퍼 메서드 안에서 위 예외 타입을 직접 던진다. 다른 42개 컨트롤러는 전부 `com.tastyhouse.core.*`를 import하지 않고 Service/Facade에 위임한다 — 이 파일이 유일한 위반이다.

### 3. `@PathVariable` 식별자 네이밍 혼재

바레(bare) `@PathVariable Long id` 사용이 77건으로 가장 많지만, `memberId`(14건), `shopId`(11건) 등 도메인 접두어 붙은 이름도 다수 공존한다. 루트 `CLAUDE.md`의 "컨트롤러 `@PathVariable` 식별자 명명 규칙" 절은 "그 컨트롤러의 주(主) 리소스는 `id`로 통일, 다른 애그리거트를 참조하는 식별자만 `{도메인}Id`로 구분"이라는 기준을 명시하고 있다. 즉 규칙 자체가 "전부 `id`로 통일"이 아니라 "주 리소스인지 아닌지로 구분"이므로, 현재의 `memberId`/`shopId` 등이 실제로 규칙 위반인지 개별 확인이 필요하다(예: `/members/{memberId}/orders/{orderId}`처럼 두 식별자를 동시에 받는 컨트롤러라면 `memberId`가 맞는 이름).

### 4. admin-api `policy` 도메인에 `response/` 부재

`admin-api/src/main/java/com/tastyhouse/adminapi/policy/`에는 `request/`가 있지만 `response/` 디렉터리가 없다. 다른 도메인은 예외 없이 양쪽을 다 가진다.

## 작업 지시

### 3-1. Facade/Service 통일

- **결정**: `Service`로 통일한다(다수파 39건 vs 2건, admin-api는 예외 없이 Service). `MemberFacade` → `MemberService`, `AuthFacade` → `AuthService`로 리네이밍(단, `auth` 도메인에 이미 다른 `AuthService`류 클래스가 있는지 먼저 확인 — 있다면 충돌 방지를 위해 병합 또는 다른 이름 검토).
- 리네이밍 후 루트 `CLAUDE.md`와 `web-api/AGENTS.md`, `admin-api/AGENTS.md`에서 "Facade"를 언급하는 부분을 "Service"로 정정(용어 통일이 목적이며 계층 구조 자체의 변경은 아님을 문서에 명시).

### 3-2. SearchApiController 위반 해소

- `validateKeyword` 검증 로직을 `SearchApiController`에서 제거하고, 해당 검증을 담당하는 Service(예: `SearchService` 또는 신규 검색 Service)로 이동.
- 컨트롤러는 이제 `com.tastyhouse.core.*`를 import하지 않아야 한다.
- 이동한 검증 로직이 기존과 동일한 `ErrorCode`를 사용해 동일한 예외를 던지는지 확인(동작 변경 없어야 함).

### 3-3. PathVariable 규칙 위반건 선별 정리

- 77건의 `@PathVariable Long id` 사용처를 전수 조사하되, **다른 애그리거트 참조가 없는 단순 주 리소스 CRUD**라면 그대로 두고(이미 규칙 준수), **다중 식별자를 받는 컨트롤러인데 전부 `id`로 뭉뚱그려 모호한 경우만** 규칙에 맞게 `{도메인}Id`로 구분.
- 반대로 `memberId`/`shopId` 등 14~11건 중, 그 컨트롤러가 실제로는 자기 리소스만 다루는 단일 식별자 CRUD인데 불필요하게 도메인 접두어를 쓴 경우는 `id`로 정리.
- CLAUDE.md 규칙 자체("주 리소스=id, 참조 식별자만 접두어")를 기준으로 하므로, **개수를 맞추는 작업이 아니라 각 컨트롤러의 실제 시그니처를 보고 판단**한다.

### 3-4. admin-api policy `response/` 보완

- `admin-api/.../policy/` 하위 응답 DTO가 현재 어디 있는지 확인(다른 패키지에 있거나, 요청과 응답을 겸용하는 record가 있을 수 있음). 없다면 신규 생성해야 하는지, 혹은 이 도메인이 원래 응답 DTO가 필요 없는 구조(예: 단순 boolean 반환)인지 확인 후 필요 시에만 `response/` 디렉터리와 record를 생성.

## 완료 기준

- [ ] `MemberFacade`/`AuthFacade`가 `Service` 네이밍으로 리네이밍됨(충돌 시 병합 방안 결정 기록)
- [ ] 문서(CLAUDE.md, 해당 AGENTS.md)의 "Facade" 언급이 "Service"로 정정됨
- [ ] `SearchApiController`가 `com.tastyhouse.core.*`를 import하지 않음, 검증 로직이 Service로 이동됨
- [ ] `@PathVariable` 규칙 위반건이 선별되어 정리됨(정리 대상·비대상 판단 근거가 기록됨)
- [ ] admin-api `policy` 도메인의 `response/` 필요 여부가 확인되고, 필요시 보완됨

## 주의사항

- Facade→Service 리네이밍은 **이름만 바꾸는 것이고 책임 구조는 그대로 유지**한다. 리네이밍 과정에서 로직을 임의로 병합하거나 나누지 않는다.
- PathVariable 정리는 개수 맞추기가 아니다 — 규칙의 판단 기준(주 리소스 vs 참조 식별자)을 각 컨트롤러마다 실제로 적용해야 한다. 잘못 판단하면 오히려 가독성이 떨어질 수 있다.
- NO_COMMIT_OR_ROLLBACK — 추천 커밋 메시지: `refactor(naming): Facade→Service 통일, SearchApiController core 의존 제거, PathVariable 네이밍 정리`.

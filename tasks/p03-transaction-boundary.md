# P3. 트랜잭션 경계 정리 — tx 없는 파사드·트랜잭션 내 외부 HTTP·ReviewCommandService CQRS 위반

## 배경

트랜잭션 경계는 원칙상 `{도메인}CommandService`(@Transactional) / `{도메인}QueryService`(@Transactional(readOnly=true))에 있고 이 배치 자체는 일관된다. 문제는 (1) 트랜잭션 없는 파사드가 한 유스케이스를 여러 트랜잭션으로 쪼개고, (2) DB 트랜잭션 안에서 외부 HTTP를 호출하며, (3) `ReviewCommandService` 한 파일에 CQRS 위반 4종이 집중돼 있다는 점이다.

## 문제 상세

### 3-1. 트랜잭션 없는 파사드가 유스케이스를 쪼갬

- `web-api/src/main/java/com/tastyhouse/webapi/member/MemberService.java` — `@Transactional` 없는 `@Service`가 8개 서비스를 조합:
  - L66-78 `updatePersonalInfo`: `memberAuthService.verifyPersonalInfoToken`(readOnly tx) → `verifyPhoneToken`(readOnly tx) → `memberCommandService.updatePersonalInfo`(write tx) = **트랜잭션 3개, 검증-갱신 비원자**
  - L80-84 `updatePassword`, L86-89 `withdrawMember`도 동일 구조
- `web-api/.../follow/FollowService.java` (`@Component`, tx 없음), `web-api/.../auth/AuthService.java` (144행, tx 없음, 7개 하위 서비스 위임) 동일 형태
- 비-CQRS 서비스가 CommandService를 주입하는 계층 우회 19곳 (`KakaoSocialLoginService` 등 소셜 로그인 4종, `CredentialLoginService`, `AuthPasswordResetService` → `MemberCommandService`)

### 3-2. DB 트랜잭션 안 외부 HTTP

- `web-api/.../payment/PaymentCommandService.java:33` 클래스 `@Transactional` → `domain-module/.../payment/domain/service/PaymentConfirmationService.java:161`의 `pgPaymentGateway.confirmPayment(...)`, `PaymentCancellationService.java:190`의 `cancelPayment(...)` — **PG HTTP 왕복 전체가 DB 트랜잭션 안**. 커넥션·락 장기 점유 + PG 성공 후 커밋 실패 시 보상 불가 불일치.
- `web-api/.../auth/service/AuthPasswordResetService.java:35-46` — `@Transactional` 안에서 `mailSender.send(...)`.

### 3-3. ReviewCommandService CQRS 위반 4종 집중

`web-api/src/main/java/com/tastyhouse/webapi/review/ReviewCommandService.java`:
- L27-29: `MemberQueryDao`, `MemberWithProfileImageResult`, `ProductDetailResult`(infra query) import
- L58-60: `ProductQueryService`(QueryService!) + `MemberQueryDao` 주입
- L137, 154: 쓰기 트랜잭션 안에서 `memberQueryDao.findMemberWithProfileImagesByIds(...)` 투영 조회
- L66/96/134/144: `ReviewResponse`/`ReviewCommentResponse` 반환 — "명령은 식별자만 반환, 컨트롤러가 QueryService 재조회" 규칙 위반
- L66/96: `ReviewCreateRequest`/`ReviewUpdateRequest` record 통째 수신 — "컨트롤러가 원시 필드로 언패킹해 전달" 규칙 위반

Response 반환 명령 추가 3건: `web-api/.../bug/BugReportCommandService.java:35`, `web-api/.../partnership/PartnershipCommandService.java:27`, `admin-api/.../shop/ShopHygieneBadgeCommandService.java:29`

## 작업 지시

1. **ReviewCommandService 정리** (우선):
   - query 의존(MemberQueryDao/ProductQueryService) 제거. 생성 시 필요한 product→shopId 역조회는 write 포트의 정당한 단건 조회(`ProductRepository.findById`)로 대체하거나, 불변식 검증 목적이면 도메인 서비스(`ReviewLifecycleService`)로 내린다.
   - 명령 메서드는 식별자(Long)만 반환하고, 컨트롤러가 `ReviewQueryService`로 재조회해 Response를 조립하도록 변경. **HTTP 응답 계약(JSON 형태)은 유지**한다.
   - Request record 수신을 원시 필드 언패킹으로 전환(컨트롤러에서 `request.title()` 등으로 풀어 전달).
   - Response 반환 명령 3건(`BugReportCommandService`/`PartnershipCommandService`/`ShopHygieneBadgeCommandService`)도 동일 패턴으로 정리.
2. **파사드 트랜잭션 경계 확정**: `MemberService.updatePersonalInfo`류의 검증→갱신 시퀀스를 원자화한다. 권장안: 파사드는 유지하되 "검증+갱신"을 하나의 CommandService 메서드(단일 @Transactional)로 내려 파사드는 위임만 하게 한다. 토큰 검증처럼 Redis 기반이라 DB 트랜잭션과 무관한 단계는 굳이 묶지 않아도 된다 — **단계별로 "DB 원자성이 실제로 필요한가"를 판정해 필요한 것만 묶고, 판정 결과를 Javadoc으로 남긴다**. `AuthService`/`FollowService`도 동일 기준 적용.
3. **트랜잭션 밖으로 외부 호출 이동**: PG 확정/취소를 reservation 도메인의 3단 구조(재시도 루프 비트랜잭션 → Executor @Transactional → 도메인 서비스, CLAUDE.md "낙관적 락 재시도 배치 규칙" 참고) 유사 패턴으로 재배치한다 — 예: ①PG 호출(tx 없음) → ②결과 반영(tx). PG 성공 후 DB 반영 실패 시의 처리(재시도/보상 큐/수동 개입 로그)를 최소 1개 장치로 남긴다. `AuthPasswordResetService`의 메일 발송은 커밋 후 발송(이벤트 AFTER_COMMIT 또는 tx 분리)으로 전환.

## 수용 기준

- [ ] `ReviewCommandService`에 `com.tastyhouse.infrastructure..query..`·`*QueryService` import 0건
- [ ] 명령 4건(review/bug/partnership/hygieneBadge)이 식별자만 반환, HTTP 응답 JSON은 기존과 동일 (컨트롤러 재조회로 보전)
- [ ] `updatePersonalInfo`류의 검증-갱신이 단일 트랜잭션이거나, 비원자 허용 근거가 Javadoc으로 명시됨
- [ ] PG confirm/cancel HTTP 호출이 DB 트랜잭션 밖에서 수행됨 + 실패 보상 장치 존재
- [ ] 관련 테스트 통과 (verify-without-gradle 절차)

## 주의사항

- **P2(ArchUnit) 태스크가 이 위반들을 red로 잡는 규칙을 추가한다** — 이 태스크가 끝나면 P2의 예외 목록에서 해당 클래스를 제거하라고 P2 담당에게 전달(또는 예외 목록의 TODO(P3) 주석 제거).
- PG 재배치는 결제 흐름이라 **동작 변경 위험이 가장 큰 작업**이다. 기존 실패 시나리오(PG 실패→롤백)가 새 구조에서도 동일하게 동작하는지 표로 대조할 것.
- CLAUDE.md의 "명령 후 재조회 2-트랜잭션 read"는 문서화된 의도 패턴이므로 문제 삼지 않는다.

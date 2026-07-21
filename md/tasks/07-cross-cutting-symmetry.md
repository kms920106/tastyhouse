# 작업지시서 07 — 횡단 관심사 비대칭 해소

## 배경 (왜)

`web-api`와 `admin-api`는 같은 "프레젠테이션 계층" 역할을 하는 두 모듈인데, 횡단 관심사(rate limiting) 적용 여부가 완전히 다르다. 이것이 의도된 설계(관리자 API는 내부용이라 rate limit이 불필요)인지, 단순히 놓친 것인지 코드만 봐서는 판단할 수 없다. 또한 `logging-module`은 두 모듈 모두가 사용하는 횡단 관심사 제공 모듈인데도 다른 5개 모듈과 달리 `AGENTS.md`가 없어 책임 범위가 문서화되어 있지 않다.

## 현재 상태 (근거)

- `web-api`: `webapi/ratelimit/` 패키지에 `@RateLimit` 어노테이션, `RateLimitAspect`(AOP), `RateLimiterService`, `RateLimitKeyType`이 구현되어 있다. `web-api/build.gradle`에 `spring-boot-starter-aop` 의존성이 있다.
- `admin-api`: rate limit 관련 패키지·클래스가 전혀 없다. `admin-api/build.gradle`에도 `spring-boot-starter-aop` 의존성이 없다.
- `logging-module`: `web`, `aop`, `security` 관련 starter를 `api`로 노출하는 횡단 모듈이지만, 루트를 포함한 6개 모듈 중 유일하게 `AGENTS.md`가 없다(다른 5개는 전부 있음).

## 작업 지시

### 7-1. Rate Limit 비대칭 결정

다음 질문에 대한 답을 결정하고 기록한다:

- Admin API는 인증된 관리자만 접근 가능하고 트래픽 규모가 일반 사용자 API와 본질적으로 다르므로(내부 소수 사용자), rate limit이 불필요한가?
- 아니면 로그인 시도 무차별 대입 공격, 관리자 계정 탈취 시나리오 등 admin-api에도 최소한의 rate limit(예: 로그인 엔드포인트만)이 필요한가?

**권장 판단 기준**: admin-api의 `auth` 도메인(로그인 엔드포인트)만큼은 무차별 대입 방어를 위한 rate limit이 필요하다고 보는 것이 일반적인 보안 관행이다. 전체 API에 rate limit을 걸 필요는 없더라도, 최소 로그인 엔드포인트는 검토 대상으로 삼는다.

- **비대칭을 의도된 것으로 유지하기로 결정한 경우**: 그 근거를 `admin-api/AGENTS.md`에 한 문단으로 명시("admin-api는 인증된 소수 관리자만 접근하는 내부 API이므로 rate limit을 적용하지 않는다" 등).
- **admin-api에도 최소 적용하기로 결정한 경우**: `admin-api/build.gradle`에 `spring-boot-starter-aop` 추가, web-api의 `ratelimit/` 패키지 구조를 참고해 admin-api에도 최소 버전(로그인 엔드포인트 대상)을 구현. 두 모듈에 동일한 `RateLimitAspect`류 코드가 중복되는 것을 감수할지, 공용화할지는 작업지시서 01의 common 정리 결정(선택지 A/B)과 일관되게 판단한다.

### 7-2. logging-module 문서화

1. `logging-module`의 실제 내용(무엇을 노출하는 모듈인지, 어떤 로깅 관련 설정/필터/유틸을 제공하는지)을 확인한다.
2. 다른 5개 모듈의 `AGENTS.md` 형식(모듈의 역할, 의존 관계, 작성 규칙)을 참고해 `logging-module/AGENTS.md`를 신규 작성한다.
3. 특히 이 모듈이 `web`, `aop`, `security` starter를 `api`로 노출하는 이유(다른 모듈들이 이 starter들에 재의존하지 않고 `logging-module` 하나만 의존하면 되도록 하는 설계인지)를 명시한다.

## 완료 기준

- [ ] Rate limit 비대칭에 대한 결정이 내려지고 근거가 기록됨(유지 또는 admin-api 확장)
- [ ] 결정이 "유지"라면 `admin-api/AGENTS.md`에 근거가 문서화됨
- [ ] 결정이 "확장"이라면 admin-api에 최소 rate limit(로그인 엔드포인트 대상)이 구현됨
- [ ] `logging-module/AGENTS.md`가 신규 작성되어 다른 모듈과 동일한 문서화 수준을 갖춤

## 주의사항

- rate limit을 admin-api에 새로 추가하는 경우, 기존 관리자 로그인 흐름이 정상 동작하는지(테스트 계정으로 로그인 반복 시 임계치 이하에서는 차단되지 않는지) 검토 필요.
- 이 작업은 다른 작업지시서와 독립적이다.
- NO_COMMIT_OR_ROLLBACK — 추천 커밋 메시지: `docs(logging-module): AGENTS.md 신규 작성` 및/또는 `feat(admin-api): 로그인 엔드포인트 rate limit 적용` (실제 결정에 따라 하나만 해당될 수도 있음).

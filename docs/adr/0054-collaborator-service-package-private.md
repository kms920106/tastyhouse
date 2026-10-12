# ADR-0054: ..service..의 협력 서비스도 package-private으로 좁히고 다른 패키지가 쓰는 30개만 허용 목록에 둔다

- 상태: 채택
- 결정 커밋: `58774e4a9` (2026-10-11)
- 대체한 결정: [ADR-0040](0040-package-private-visibility.md)(후속 과제 행 — "UseCase 없는 도메인 서비스 약 48개")

## 맥락

- **협력 서비스**: `..service..` 패키지에 있지만 인바운드 포트(UseCase)를 구현하지 않는 클래스입니다. 도메인 서비스·Reader·Validator처럼 유스케이스 서비스가 함께 쓰는 빈입니다.
- **split package**: 같은 자바 패키지를 여러 Gradle 모듈이 나눠 쓰는 상태입니다. 이 저장소의 `com.tastyhouse.application..`은 코어와 앱 모듈 4개가 나눠 씁니다.

[ADR-0040](0040-package-private-visibility.md)은 UseCase 구현 서비스를 package-private으로 좁혔지만, 가드 `useCaseImplementationsShouldNotBePublic`은 UseCase 구현만 검사했습니다. 그래서 협력 서비스는 가드 밖에 있는 빈틈이었습니다(커밋 메시지).

## 이전 규칙

### backend/CLAUDE.md — 접근 제어자 규칙 (내부 구현은 package-private)

`public` 유지 대상 표에 "UseCase 없는 도메인 서비스(약 48개는 다른 패키지에서 import) — `application` 5모듈" 행이 있었고, 이것은 후속 과제로 남아 있었습니다.

같은 표의 `SpringDomainEventPublisher` 행(다른 패키지의 `SharedBeanConfig`가 참조)에는 `ProhibitedWordValidator`·`CachingProhibitedWordLoadPort`도 함께 들어 있었습니다.

## 결정

`..service..`의 협력 서비스도 package-private으로 좁힙니다. 다른 패키지가 import하는 것만 허용 목록 `PUBLIC_BY_NECESSITY`로 관리합니다.

| 항목 | before | after |
|---|---|---|
| 협력 서비스(`..service..`의 UseCase 비구현 class) 103개 | `public`, 가드 밖 | 다른 패키지가 참조하지 않는 73개는 package-private |
| 다른 패키지가 import하는 협력 서비스 | 후속 과제 "약 48개" | 30개 — 허용 목록 `PUBLIC_BY_NECESSITY`(사유 표와 함께) |
| `ProhibitedWordValidator`·`CachingProhibitedWordLoadPort` | `SpringDomainEventPublisher` 행에 동거 | `..service..`에 있어 협력 서비스 30개 행·`PUBLIC_BY_NECESSITY`로 이동 |
| 같은 패키지의 모듈 간 참조(split package 20건) | — | package-private으로 성립하므로 허용 목록 등재 사유가 아니다 |
| 가드 | `useCaseImplementationsShouldNotBePublic`(UseCase 구현만) | 추가: `LayerRulesTest#serviceClassesShouldNotBePublic`(하한 650) + `publicByNecessityShouldStillBePublic`(허용 목록 낡음 검사) |

## 근거

- 가드가 UseCase 구현만 보면 같은 패키지의 협력 서비스는 아무 제약 없이 `public`으로 남아, 다른 패키지가 구현에 직접 결합할 수 있었습니다.
- 다른 패키지가 쓰는 협력 서비스(리스너→`PointLedgerService`, ceo의 `product.service`·`review.service`→`shop.service.ShopOwnershipValidator`, 다른 컨텍스트 서비스→`PublicHolidayCalendar` 등)는 컴파일을 위해 `public`이어야 하므로 이름과 사유를 목록으로 관리합니다.
- split package는 패키지가 같으므로 모듈이 달라도 package-private 접근이 성립합니다.

## 결과·감수한 손실

- 선언 키워드만 바꿨고 동작 변경은 없습니다.
- `CeoUserDetails`(`auth/security`)는 `..service..` 밖이라 이 규칙의 대상이 아닙니다.
- 허용 목록은 줄어드는 방향으로만 관리합니다. 낡은 항목은 `publicByNecessityShouldStillBePublic`이 잡습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · 접근 제어자 규칙 (내부 구현은 package-private) · 협력 서비스 30개 행과 `PUBLIC_BY_NECESSITY`
- `backend/application-core/AGENTS.md` · UseCase 구현 서비스·리스너·설정에 `public`을 붙이지 않는다 (package-private 적용, 5모듈 공통 정본) · 허용 목록 사유 표
- `backend/{web,admin,ceo}-application/AGENTS.md` · "규칙" · 협력 서비스 가시성 서술

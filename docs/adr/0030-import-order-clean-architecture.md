# ADR-0030: import 순서를 클린 아키텍처 원 기준으로 재정의한다(domain 접두어는 전부 1순위)

- 상태: 채택
- 결정 커밋: `1e3818c5b` (2026-09-27)
- 대체한 결정: —

## 맥락

이 저장소는 Spring 공식 import 그룹 순서(`java` → `javax` → 서드파티 → 자사 → static) 위에, **자사 그룹(`com.tastyhouse.*`) 내부를 계층 순으로 정렬하는 커스텀 규칙**을 얹어 씁니다. 안정 의존성 원칙(의존은 안쪽 domain을 향한다)에 따라 가장 안정적인 코드를 맨 위에 둡니다.

그런데 덩어리 03a/03b로 포트·서비스·리스너가 옮겨진 뒤에도 기존 분류표는 예전 구조에 맞춰져 있었습니다.

- `domain.shared`·`domain.exception`이 도메인 모델보다 아래(4순위 "external/shared")에 분류돼 있었습니다.
- 1순위 매칭에 하위 패키지(`.repository`·`.service`·`.port`)가 나열돼 있었습니다.
- 이미 삭제된 `.converter`가 남아 있었습니다.
- `apicommon` 세그먼트가 표에 없었습니다.
- "4개 그룹"이라는 표기와 실제 5개 그룹이 모순됐습니다.

기존 코드 1,101개 파일이 규칙 위반 상태였습니다.

## 이전 규칙

### backend/CLAUDE.md — 코딩 스타일 (import 순서) / 자사 코드 그룹 내부 계층 정렬 (헥사고날 안→밖: domain이 위)

자사 그룹 내부를 `(계층 순위, 알파벳)` 복합 키로 정렬했다.

| 순위 | 계층 | 매칭 패키지 |
|---|---|---|
| 1 | domain | `com.tastyhouse.domain.<ctx>.model` / `.vo` / `.event` / `.repository` / `.service` / `.port` |
| 2 | application | `com.tastyhouse.application.*` |
| 3 | infrastructure | `com.tastyhouse.infrastructure.<ctx>.query` / `.persistence`(`.converter`) |
| 4 | external / shared | `com.tastyhouse.external.*`, `security.*`, `logging.*`, **`domain.shared.*`, `domain.exception.*`** |
| 5 | presentation | `webapi.*` · `adminapi.*` · `ceoapi.*` · `batch.*` |

`com.tastyhouse.external.*`는 모듈이 바뀌어도 패키지가 불변이라 4순위를 유지했고, 리네임된 코어 `com.tastyhouse.restclient.*`도 4순위에 추가했다. 문서 서두는 import를 "4개 그룹"으로 나눈다고 적었지만 실제로는 static까지 5개를 나열했다.

## 결정

자사 import 순위를 클린 아키텍처 원(Domain 중심 → Application → 바깥 원) 기준 5단계로 재정의합니다. **판별은 `com.tastyhouse.` 바로 다음 최상위 세그먼트 하나로만** 합니다.

| 항목 | before | after |
|---|---|---|
| 판별 기준 | 최상위 + 하위 패키지(`.model`·`.repository`·`.port` 등) 혼용 | `com.tastyhouse.` 다음 최상위 세그먼트 하나 |
| `domain.shared`·`domain.exception` | 4순위(external/shared) | 1순위(접두어가 `domain`이면 전부) |
| 3순위 | `infrastructure`(`.converter` 포함) | driven 어댑터 `infrastructure`(+ 당시 `external`·`restclient`) |
| 4순위 | external·security·logging·domain.shared·domain.exception | 공유 횡단 모듈 `apicommon`·`logging`·`security`(+ 테스트 전용 세그먼트) |
| 5순위 | presentation | driving 어댑터 `adminapi`·`batch`·`ceoapi`·`webapi`(5-a 공용 → 5-b 도메인 전용) |
| 그룹 수 표기 | "4개 그룹"(실제 5개 나열) | 5개 그룹 |
| 모듈별 등장 가능 순위 | 없음 | 표로 명시(어긋나면 레이어 위반 신호) |
| 기존 위반 파일 | 서술 없음 | 1,101개 위반을 명시하고 옆 파일을 근거로 삼지 않게 함 |

## 근거

- 판별을 최상위 세그먼트 하나로 고정하면 파일마다 사람이 판단할 여지가 사라집니다. 클래스명 접미어나 하위 패키지를 보지 않습니다.
- 03a/03b로 domain 하위 구조가 바뀌어 하위 패키지 나열이 낡았습니다. `domain.exception`을 도메인 모델보다 아래에 두던 분류는 "접두어가 domain이면 안쪽 원"이라는 원칙과 맞지 않았습니다.
- 모듈 의존 방향 때문에 모듈마다 등장할 수 있는 순위가 정해져 있으므로, 순위가 맞지 않는 import는 순서 문제가 아니라 레이어 위반일 가능성이 큽니다.

## 결과·감수한 손실

- 이 커밋은 문서만 바꿨습니다. 후속 `f5122d837`(2026-09-27)이 위반 파일 1,101개의 import 블록만 재정렬하고, `domain` 테스트에 `ImportOrderConventionTest` 가드를 신설해 `./gradlew build`에서 강제했습니다. 순위 표에 없는 세그먼트는 실패시키고, 스캔 파일 수가 3,000개를 넘는지도 확인합니다.
- import만 바꾼 변경은 바이트코드가 같아 `:domain:test`가 UP-TO-DATE로 건너뛰어집니다. 그래서 `domain/build.gradle`에 backend `*.java`를 테스트 입력으로 선언했습니다.
- 이 절 제목은 `ImportOrderConventionTest` 메시지가 가리키므로 제목과 순위 표 구조를 유지합니다. 순위 표는 `domain/AGENTS.md` 봉인 목록의 동기화 규칙에 따라 3곳이 맞아야 합니다.
- 이후 [ADR-0037](0037-infrastructure-package-root.md)로 `external`·`restclient` 세그먼트가 사라져 3순위는 `infrastructure` 하나가 됐습니다. [ADR-0031](0031-persistence-domain-reallowed.md)로 `infrastructure:jpa`의 쓰기 어댑터에는 1순위 import가 다시 등장할 수 있게 됐습니다.

## 영향받은 문서

- `backend/CLAUDE.md` · "코딩 스타일 (import 순서)" · 5개 그룹과 빈 줄 규칙
- `backend/CLAUDE.md` · "자사 그룹 내부 계층 정렬 (클린 아키텍처 원: 안 → 밖)" · 최상위 세그먼트 하나로 판별하는 5단계 순위 표, 모듈별 등장 가능 순위, `domain.exception`도 1순위
- `backend/CLAUDE.md` · "예시 (현재 코드에 실제로 있는 import)" · 실제 코드의 before/after 예시
- `backend/AGENTS.md` · "Working In This Directory" · import 순서 요약(application 순위 포함)
- `backend/domain/AGENTS.md` · "봉인·가드 목록" · `ImportOrderConventionTest`와 순위 표 3곳 동기화 규칙

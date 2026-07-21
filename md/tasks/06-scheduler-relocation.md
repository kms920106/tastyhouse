# 작업지시서 06 — 스케줄러 책임 재배치

## 배경 (왜)

`web-api`는 프레젠테이션 모듈(HTTP 요청/응답 처리)이어야 하는데, 실제로는 배치성 유스케이스인 스케줄러 5개 클래스를 보유하고 있다. 이들은 HTTP 요청과 무관하게 시간 기반으로 동작하는 로직이므로, 프레젠테이션 계층의 책임 범위를 벗어난다. `admin-api`에는 대응하는 스케줄러가 전혀 없어(비대칭), 왜 web-api에만 배치 작업이 몰려 있는지도 불명확하다.

## 현재 상태 (근거)

`web-api/src/main/java/com/tastyhouse/webapi/scheduler/`에 다음 클래스들이 있다:

- `RankScheduler`
- `ProductScheduler`
- `GradeScheduler` + `GradeSchedulerService`
- `SearchKeywordScheduler`

`@EnableScheduling`은 `WebApiApplication`(web-api의 메인 애플리케이션 클래스)에 붙어 있다. `admin-api`에는 스케줄러 패키지 자체가 없다.

## 작업 지시

### 6-1. 선택지 비교 및 결정

다음 두 선택지를 비교해 결정한다. 결정 근거를 이 문서 하단에 기록한다.

- **선택지 A (web-api 잔류, 얇게 정리)**: 스케줄러를 web-api에 그대로 두되, 각 스케줄러 클래스가 직접 리포지토리/QueryDSL을 호출하는 부분이 있다면(있는지 확인 필요) 전부 core-module의 `application` 서비스 호출로 교체한다. 즉 스케줄러는 "언제 실행할지"만 담당하고 "무엇을 할지"는 core에 위임하는 순수 트리거로 축소.
- **선택지 B (별도 배치 모듈 신설)**: `batch-module` 또는 `scheduler-module`을 신설해 5개 스케줄러를 이동시키고, `core-module`(implementation)과 `infrastructure-module`(runtimeOnly)에만 의존하게 한다. web-api/admin-api 어느 쪽에도 속하지 않는 독립 실행 단위가 된다.

**권장**: 선택지 A. 이유는 (1) 스케줄러 5개는 규모가 크지 않아 별도 모듈 신설의 이익(독립 배포/스케일링)이 크지 않고, (2) `@EnableScheduling` + `WebApiApplication`이라는 기존 실행 방식을 유지하면 배포 파이프라인 변경이 필요 없다. 다만 사용자가 향후 배치 서버를 웹 서버와 분리 배포할 계획이 있다면 선택지 B가 맞다 — 이 결정은 배포 전략과 연결되므로 작업 착수 전 확인 권장.

### 6-2. 선택지 A 실행 시 세부 작업

1. `RankScheduler`, `ProductScheduler`, `GradeScheduler`, `GradeSchedulerService`, `SearchKeywordScheduler` 각각을 열어, core-module의 리포지토리 인터페이스나 QueryDSL을 직접 호출하는 부분이 있는지 확인한다.
2. 있다면 해당 로직을 core-module의 대응 도메인 `application` 서비스(예: `RankCommandService`, `ProductScheduler`가 다루는 도메인의 서비스 등)에 메서드로 옮기고, 스케줄러는 그 서비스를 호출만 하도록 축소한다.
3. `GradeSchedulerService`처럼 별도 Service 클래스로 분리된 경우, 이미 좋은 패턴이므로 다른 스케줄러들도 동일하게 "Scheduler(트리거) + Service(로직)" 이분 구조로 통일할 수 있는지 검토한다.

## 완료 기준

- [ ] 선택지 A/B 결정이 기록됨(배포 전략 확인 여부 포함)
- [ ] (선택지 A 채택 시) 5개 스케줄러가 core-module 리포지토리/QueryDSL을 직접 호출하지 않고, core `application` 서비스 호출로만 구성됨
- [ ] 스케줄러 클래스들의 트리거(Scheduler)와 로직(Service) 분리가 일관됨
- [ ] admin-api와의 비대칭이 "의도된 것"으로 문서화됨(admin은 배치 작업이 필요 없는 도메인 특성이라는 점을 이 작업지시서 또는 AGENTS.md에 명시)

## 주의사항

- 스케줄 주기(cron 표현식 등)는 절대 변경하지 않는다 — 순수 구조 리팩터링.
- 이 작업은 다른 작업지시서와 의존관계가 없어 독립적으로 아무 때나 진행 가능하다.
- NO_COMMIT_OR_ROLLBACK — 추천 커밋 메시지: `refactor(scheduler): 스케줄러의 도메인 로직을 core-module application 서비스로 위임`.

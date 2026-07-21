# batch-module

시간 기반 배치 유스케이스(스케줄러)를 전담하는 **독립 실행 모듈**. `web-api`/`admin-api`와 동일하게 자체 `main()`(`BatchApplication`)을 가진 bootJar 실행 단위이며, HTTP 요청과 무관하게 `@Scheduled` cron 트리거로만 동작한다.

## 신설 배경

기존에는 스케줄러 5개(`RankScheduler`/`ProductScheduler`/`GradeScheduler`+`GradeSchedulerService`/`SearchKeywordScheduler`)가 프레젠테이션 모듈인 `web-api`에 있었다. 웹 서버와 배치 서버를 분리 배포하기 위해 이 모듈로 이동했다(작업지시서 `md/tasks/06-scheduler-relocation.md` 선택지 B).

## 패키지 구조

```
com.tastyhouse.batch/
├── BatchApplication.java          @SpringBootApplication + @EnableScheduling (진입점)
├── scheduler/                     Scheduler(트리거) + Service(로직) 이분 구조
│   ├── RankScheduler.java             — core RankCommandService만 호출하는 순수 트리거(로직 없음)
│   ├── SearchKeywordScheduler.java    — core SearchKeywordCommandService만 호출하는 순수 트리거(로직 없음)
│   ├── GradeScheduler.java            — 트리거만
│   ├── GradeSchedulerService.java     — 회원 등급 계산 로직
│   ├── ProductScheduler.java          — 트리거만 (@Scheduled 주석 처리된 비활성 상태 유지)
│   └── ProductSchedulerService.java   — BBQ 옵션 크롤링 저장 로직
└── crawling/bbq/                  ProductSchedulerService가 의존하는 BBQ 크롤링 어댑터
    ├── BbqService.java
    └── response/*.java
```

## 규칙

- **Scheduler(트리거) + Service(로직) 이분 구조**: `@Scheduled` 메서드를 가진 클래스는 cron 트리거와 try/catch 로깅만 담당하고, 실제 도메인 로직은 core `application` 서비스를 호출하는 별도 `XxxSchedulerService`(또는 core 서비스 직접 호출)로 위임한다. 로직이 core 서비스 한 줄 호출로 충분하면(`RankScheduler`/`SearchKeywordScheduler`) 별도 Service 분리 없이 트리거가 core 서비스를 직접 호출해도 이분 구조의 이상형에 부합한다.
- **core-module 리포지토리/QueryDSL 직접 호출 금지**: 모든 스케줄러는 core `application` 서비스(Command/Query Service)만 호출한다. 리포지토리 인터페이스나 QueryDSL을 스케줄러/배치 Service에서 직접 호출하지 않는다.
- **cron 표현식은 순수 구조 리팩터링 대상이 아니다**: 스케줄 주기를 바꾸는 변경은 이 모듈이 아니라 별도 운영 결정으로 다룬다.
- **단일 인스턴스 배포 전제**: `@EnableScheduling` 기반 cron은 인스턴스마다 독립 실행된다. batch-module을 여러 인스턴스로 배포하면 동일 작업이 중복 실행되므로, 운영 시 배치 인스턴스는 1대로 유지한다(분산 락 등 중복 방지 로직은 아직 없음).

## admin-api 비대칭 (의도된 설계)

`admin-api`에는 배치 스케줄러가 없다. `admin-api`는 관리자용 CRUD/조회 API만 제공하며, 시간 기반으로 자동 실행되어야 하는 배치 유스케이스(랭킹 집계, 등급 갱신, 인기 검색어 집계, 상품 옵션 동기화)가 전부 사용자(web) 도메인에 속하기 때문이다. 배치 책임은 web-api가 아니라 이 `batch-module`로 일원화되어 있다.

## Dependencies

### Internal
- `core-module` (implementation) — application 서비스, 도메인 모델
- `infrastructure-module` (runtimeOnly) — JPA 어댑터(컴파일 타임에는 은닉)
- `external-api` (implementation) — `BbqApiClient`(크롤링 HTTP 클라이언트), `RemoteImageDownloader`
- `logging-module` (implementation)

### External
- Spring Boot Starter(루트 `subprojects`가 부여) — `@Scheduled`/`@Transactional` 지원
- Lombok

## 설정 파일

`src/main/resources/application.yml`이 `application-infrastructure.yml`(DB/JPA, infrastructure-module 소유)과 `application-external.yml`(크롤링/S3, external-api 소유)을 `classpath:` import한다 — web-api와 동일한 패턴. 웹 전용 설정(서버 포트/CORS/JWT/OAuth/Redis/multipart)은 없다.

<!-- MANUAL: -->

# notice 도메인 전환 — **재작업** (query DAO infra 이관 + CQRS 서비스 분리 + apt 롤백)

> 선행: `tasks/README.md`(개정본) 숙독 + `00-phase0` 완료. 그룹 1 중 **최우선 수행** — 전 모듈 공통의 빌드 정리(apt 롤백·querydsl-jpa 강등·ArchUnit 규칙 추가)가 이 문서에 귀속되어 있다.

## 현황 (구 패턴으로 1차 전환 완료된 상태)
- core notice: `application/` 삭제 완료, `NoticeRepository`는 `findById`+`save`만(write 순수화 완료), `Notice`는 순수 POJO. infra `NoticeRepositoryImpl`도 write 전용. — **이 부분은 재작업 불필요.**
- **문제 지점 (구 패턴 3의 산물)**: query DAO가 api 모듈에 있어 QueryDSL이 api 모듈에 노출됨.
  - `admin-api/.../adminapi/notice/query/` — `NoticeQueryDao`(JPAQueryFactory·`QNoticeJpaEntity` 직접 사용), `NoticeListItemResult`·`NoticeDetailResult`(@QueryProjection), `NoticeSearchCondition`
  - `web-api/.../webapi/notice/query/` — `NoticeQueryDao`(visible=true 고정 조회), `NoticeListItemResult`(@QueryProjection)
  - admin `NoticeService`가 command+query 혼재 단일 서비스(@Transactional readOnly 기본 + 쓰기 오버라이드).

## 작업

### 1. infra query 패키지 신설 (패턴 3)
`infrastructure-module/src/main/java/com/tastyhouse/infrastructure/notice/query/`에 5파일 신설:

| 파일 | 내용 |
|---|---|
| `NoticeQueryDao.java` | @Repository. 메서드 3개 — `findAllNotices(NoticeSearchCondition, PageQuery)` → `PageResult<NoticeManagementListItemResult>`(현 admin DAO 이식), `findDetailById(Long)` → `Optional<NoticeDetailResult>`(현 admin DAO 이식), `findVisibleNotices(PageQuery)` → `PageResult<NoticeListItemResult>`(현 web DAO 이식). BooleanExpression private 헬퍼 유지 |
| `NoticeManagementListItemResult.java` | 현 admin `NoticeListItemResult`(id, title, content, visible, createdAt)를 **리네이밍 이식** — web 형제와 같은 패키지에 공존해 충돌하므로 `Management` 한정어 부여 |
| `NoticeListItemResult.java` | 현 web 버전(id, title, content, createdAt) 그대로 이식 |
| `NoticeDetailResult.java` | 현 admin 버전 그대로(형제 없음 → 순수명) |
| `NoticeSearchCondition.java` | 현 admin 버전 그대로(of 팩토리 유지) |

### 2. api 모듈 서비스 CQRS 분리 (패턴 2)
- **admin-api**: `adminapi/notice/NoticeService`를 분해 —
  - `NoticeCommandService`(@Transactional): `NoticeRepository`(domain write 포트) 주입, create/update/delete(NoticeId 승격 규칙 유지).
  - `NoticeQueryService`(@Transactional(readOnly = true)): infra `NoticeQueryDao` 주입, getNotices/getNotice + Response 조립 private 매퍼. import는 `com.tastyhouse.infrastructure.notice.query.*`, 타입은 `NoticeManagementListItemResult`로 치환.
  - `NoticeApiController`: 두 서비스를 각각 주입하도록 수정(HTTP 계약 무변경).
- **web-api**: `webapi/notice/NoticeService` → `NoticeQueryService`로 리네이밍(조회 전용 도메인 — QueryService만 둔다), infra DAO 주입으로 교체, `NoticeApiController` 주입 수정.

### 3. api 모듈 query 패키지 삭제
- `admin-api/.../adminapi/notice/query/` 4파일, `web-api/.../webapi/notice/query/` 2파일 전부 삭제(패키지째).

### 4. 빌드 정리 (전 모듈 공통 — 이 작업에 귀속)
- **build.gradle 4개**(web-api/admin-api/ceo-api/batch-module): Phase 0에서 추가된 querydsl apt(annotationProcessor `querydsl-apt`·`jakarta.annotation-api`)와 querydsl sourceSets/generated 디렉터리 블록 **전부 제거**(롤백).
- `infrastructure-module/build.gradle`: `querydsl-jpa`를 `api` → `implementation` **강등**(강등 직전 grep으로 api 4모듈의 `com.querydsl` import 0건 선확인). core의 `querydsl-core`(api 노출)는 99-finalize에서 제거 — 그 전까지 완전 차단은 ArchUnit이 담당.
- 각 api 모듈의 `build/generated/sources/annotationProcessor` stale 잔재 디렉터리 삭제(gradle 실행 아님) 후 LSP 재인덱싱.

### 5. ArchUnit 규칙 추가 (00-phase0 §5 개정분 실행)
- web/admin/ceo `architecture/LayerRulesTest`에 규칙 2개 추가: `com.querydsl..` 의존 금지, `..infrastructure..persistence..` 의존 금지.
- batch-module에 `LayerRulesTest` 신설(archunit `testImplementation` 추가) — 동일 규칙.

## 완료 기준
- core notice에 `domain/`만 잔존(변화 없음), infra에 `notice/query/` 신설, api 모듈에 `notice/query/` 패키지 부재.
- grep 검증: api 4모듈 `com.querydsl` 0건 / `QueryProjection` 0건 / build.gradle `querydsl-apt` 0건 / `adminapi.notice.query|webapi.notice.query` 참조 0건.
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시 (예: `refactor(notice): notice 조회 DAO를 infrastructure-module로 이관 — api 모듈 QueryDSL 의존 차단`, `chore(build): api 모듈 querydsl apt 롤백·infra querydsl-jpa implementation 강등·ArchUnit 규칙 추가`).

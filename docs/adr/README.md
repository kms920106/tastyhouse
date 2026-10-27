# 결정 기록 (ADR)

규칙 문서(루트 `CLAUDE.md`, `backend/CLAUDE.md`, `backend/**/AGENTS.md`, `docs/domain/*.md`)의 **결정 경위**를 담는 곳입니다. 규칙 문서 본문은 **현재 규칙만** 서술하고, "예전에는 어땠고 왜 바뀌었는가"는 여기에 남깁니다.

## 왜 분리했나

규칙 문서에 지난 규칙과 바뀐 규칙이 취소선·이력 꼬리표로 한 문장 안에 겹겹이 쌓이면서(한 문장에 3단 중첩, 모듈 수 연대기 중간에 끼어든 과거 값 등) **현재 규칙을 찾는 비용이 이력을 읽는 비용에 묻혔습니다.** 2026-10-12 기준 43개 문서 663줄에 그런 표기가 있었고, 이를 결정 단위로 걷어 이 디렉터리의 ADR 54개로 옮겼습니다.

ADR(Architecture Decision Record)은 "결정 1건 = 파일 1개"로 맥락·결정·근거·결과를 적는 기록 형식입니다.

## 작성 규칙

- **파일명**: `NNNN-{kebab-slug}.md`. 번호는 결정이 처음 문서에 들어간 커밋 시각 순으로 이어 붙입니다(새 결정은 마지막 번호 + 1). slug는 영문 소문자·숫자·하이픈이며 결정 내용을 나타냅니다.
- **한 파일 = 결정 하나.** 결정이 앞선 결정을 덮으면 새 ADR을 만들고, 덮인 ADR의 상태를 `대체됨(→ 새 번호)`로 바꿉니다. 덮인 ADR의 본문은 지우지 않습니다.
- **규칙 문서 본문은 덮어씁니다.** 취소선·이력 꼬리표를 남기지 않고 현재 규칙으로 고칩니다. 경위가 현재 규칙 이해에 꼭 필요한 곳에만 `([ADR-NNNN](…/docs/adr/NNNN-slug.md))` 링크를 한 번 답니다. 경로는 링크를 다는 문서 기준 상대경로입니다.
- **다른 문서·테스트 메시지가 참조하는 제목은 유지합니다.**
- **영향받은 문서**는 역참조 앵커 3요소(리포 루트 기준 경로 · 절 제목 · 원문 취지)로 적고 줄 번호는 쓰지 않습니다.

템플릿:

```markdown
# ADR-NNNN: {결정 제목}

- 상태: 채택 | 대체됨(→ ADR-MMMM)
- 결정 커밋: `{해시 9자리}` ({YYYY-MM-DD})
- 대체한 결정: ADR-KKKK (없으면 —)

## 맥락
## 이전 규칙
## 결정
## 근거
## 결과·감수한 손실
## 영향받은 문서
```

## 잔존 검사

규칙 문서 본문에 취소선·이력 꼬리표(지운 결정 표시, `(개정 — …)`, `(…후 갱신)`, `당시 이름` 등)가 다시 들어오지 않았는지 확인합니다. 리포 루트에서 실행하며 **기대값은 0줄**입니다(이 디렉터리는 이력을 담는 곳이라 검사에서 뺍니다).

```bash
grep -rn --include='*.md' -e '번복' -e '~~' -e '후 갱신)' -e '(개정' -e '개정 —' -e '당시 이름' -e '당시 기록' \
  CLAUDE.md backend docs/domain docs/workflow | grep -v '/bin/'
```

## 색인

| ADR | 결정 | 상태 | 결정 커밋 | 대체한 결정 |
|---|---|---|---|---|
| [0001](0001-domain-framework-free.md) | domain 모듈을 프레임워크-프리로 만들고 공유 VO에서도 JPA 어노테이션을 걷어낸다 | 채택 | `4f82b1ea7` 2026-07-21 | — |
| [0002](0002-file-url-resolved-in-query-dao.md) | 파일 경로→URL 변환은 Service가 아니라 query DAO가 투영 시점에 끝낸다 | 채택 | `70eb33376` 2026-08-02 | — |
| [0003](0003-api-common-shared-plumbing.md) | 3개 api 모듈에 똑같이 복제된 플럼빙은 api-common-module이 단독 소유한다 | 채택 | `f15e0b786` 2026-08-02 | — |
| [0004](0004-entity-fk-raw-long-idmapping.md) | 엔티티 FK는 raw Long으로 두고 매퍼에서 IdMapping으로 승격·언패킹한다(정책 B) | 대체됨(→ 0028) | `f37028ab4` 2026-08-06 | — |
| [0005](0005-inbound-port-full-mapping.md) | 인바운드 포트(UseCase 인터페이스)를 도입한다 — 완전 매핑 전략 | 채택 | `01d6d7494` 2026-08-29 | — |
| [0006](0006-request-to-command-in-controller.md) | Request→Command 변환은 Request의 toCommand로 컨트롤러가 소유한다 | 채택 | `01d6d7494` 2026-08-29 | — |
| [0007](0007-application-common-module-read-ports.md) | 읽기 계약을 application-common-module에 모으고 application은 QueryPort로만 읽는다 | 대체됨(→ 0009) | `c50e39780` 2026-08-29 | — |
| [0008](0008-per-app-application-modules.md) | 앱별 application 계층을 api 모듈에서 떼어 {앱}-application 모듈 4개로 분리한다 | 대체됨(→ 0013) | `c55520733` 2026-08-30 | — |
| [0009](0009-read-contracts-split-by-consumer.md) | 읽기 계약을 소비 앱 수로 나눠 다중 앱 공유분은 domain이, 단독분은 그 앱 모듈이 소유한다 | 대체됨(→ 0015) | `2b583d0c2` 2026-08-30 | 0007 |
| [0010](0010-api-domain-free-carve-outs.md) | api 모듈은 domain을 금지하되 domain.exception·domain.shared.page·enum 읽기 accessor 3종만 carve-out한다 | 대체됨(→ 0025) | `3bf8f9171` 2026-09-01 | — |
| [0011](0011-rate-limit-port-in-api-common.md) | RateLimitCounterPort를 api-common-module로 올려 redis→api-common 방향으로 역전한다 | 대체됨(→ 0023) | `8bcf3e2f3` 2026-09-01 | — |
| [0012](0012-response-from-whole-result.md) | Response.from은 읽기 계약 XxxResult를 통째로 받는다 | 채택 | `787dd580f` 2026-09-01 | — |
| [0013](0013-application-module-merge.md) | 앱별 application 모듈 4개를 application 하나로 합친다(챕터 01) | 대체됨(→ 0036) | `68d160710` 2026-09-04 | 0008 |
| [0014](0014-package-flattening-app-markers.md) | application 패키지를 평탄화하고 앱 소속을 마커 애노테이션으로 표현한다(챕터 03) | 대체됨(→ 0036) | `9b888f1c5` 2026-09-04 | — |
| [0015](0015-shared-read-contracts-in-application.md) | 공유 읽기 계약 55개를 domain에서 application으로 되돌린다(챕터 04) | 채택 | `b5ad5e5be` 2026-09-04 | 0009 |
| [0016](0016-external-vendor-channel-split.md) | infrastructure:external을 벤더·채널별 모듈로 분리한다(기술별 분할 채택) | 채택(부분 → 0021·0022) | `109fb06de` 2026-09-05 | — |
| [0017](0017-library-auto-configuration.md) | 라이브러리 모듈을 Spring Boot auto-configuration으로 자기 등록한다(챕터 02) | 대체됨(→ 0038) | `cad5e5880` 2026-09-05 | — |
| [0018](0018-channel-starter-vendor-modules.md) | 외부 연동은 코드 없는 채널 스타터 + 벤더 구현 모듈로 세분한다 | 채택 | `ae9870cd4` 2026-09-05 | — |
| [0019](0019-token-store-port-inversion.md) | 토큰 저장소 6종을 security-core 포트 + infrastructure:redis 어댑터로 역전한다 | 채택 | `8cba65373` 2026-09-06 | — |
| [0020](0020-java-comments-forbidden.md) | backend java에는 주석을 쓰지 않고 근거는 AGENTS.md·docs/domain이 소유한다 | 채택 | `104b04d74` 2026-09-22 | — |
| [0021](0021-file-storage-spi-removal.md) | external 코어의 파일 저장 SPI를 지우고 벤더가 FileStoragePort를 직접 구현한다 | 채택 | `13127bc9f` 2026-09-26 | 0016(일부) |
| [0022](0022-aws-module-three-way-split.md) | infrastructure:aws를 aws-s3·aws-ses·aws-sns 3모듈로 나눈다 | 채택 | `15e76e263` 2026-09-26 | 0016(일부) |
| [0023](0023-rate-limit-port-in-security-core.md) | RateLimitCounterPort를 security-core로 옮겨 redis→표현 계층 의존을 끊는다 | 채택 | `8485c0e8a` 2026-09-27 | 0011 |
| [0024](0024-event-listeners-in-application.md) | 도메인 이벤트 리스너는 persistence가 아니라 application에 둔다 | 채택(@SharedApp 부분 → 0036) | `59285a4e7` 2026-09-27 | — |
| [0025](0025-presentation-domain-cut.md) | 표현 계층(api·api-common·security)이 domain을 모르게 절단한다(엄격 레이어드 덩어리 01) | 채택(ErrorContracts → 0042, @SharedApp config → 0036) | `dfb8f4685` 2026-09-27 | 0010 |
| [0026](0026-vendor-ports-to-application.md) | 벤더 16모듈의 domain 의존을 끊고 벤더 포트·서비스를 application으로, 등록은 application config(PgRouterConfig·Mail/SmsServiceConfig)로 옮긴다(덩어리 02) | 대체됨(→ 0034) | `d98b46c0c` 2026-09-27 | — |
| [0027](0027-domain-ports-to-application.md) | 나머지 포트·포트 주입 도메인 서비스·이벤트 발행기를 application으로 옮기고 등록은 <Ctx>ServiceConfig가 한다(덩어리 03a) | 대체됨(→ 0034) | `ef957e488` 2026-09-27 | — |
| [0028](0028-persistence-state-layer.md) | persistence가 domain을 모르게 State·StatePort·Store 계층을 둔다(덩어리 03b) | 대체됨(→ 0031) | `cdd6c7727` 2026-09-27 | 0004 |
| [0029](0029-no-suppress-warnings.md) | 경고를 @SuppressWarnings로 가리지 않는다(XxxCodes 복제 상수 삭제·SensitiveFieldMasker 배선) | 채택 | `d555b0325` 2026-09-27 | 0028(XxxCodes 부분) |
| [0030](0030-import-order-clean-architecture.md) | import 순서를 클린 아키텍처 원 기준으로 재정의한다(domain 접두어는 전부 1순위) | 채택 | `1e3818c5b` 2026-09-27 | — |
| [0031](0031-persistence-domain-reallowed.md) | persistence가 도메인 모델을 직접 쓰도록 State 계열을 제거한다(쓰기 경로만, 조회 DAO는 domain-free 유지) | 채택 | `9d6f4a1ba` 2026-09-28 | 0028 |
| [0032](0032-outbound-port-adapter-naming.md) | 쓰기 포트·구현 이름을 PersistencePort·PersistenceAdapter·QueryAdapter로 통일한다 | 채택(PersistencePort → 0046에서 세분) | `64b985bba` 2026-09-30 | — |
| [0033](0033-usecase-interface-injection.md) | 서비스 간 협력도 UseCase 인터페이스로 주입하고 구체 클래스를 주입하지 않는다 | 채택 | `9b0582c33` 2026-10-01 | — |
| [0034](0034-service-config-removal.md) | application *ServiceConfig를 지우고 도메인 서비스 클래스에 앱 마커만 단다 | 대체됨(→ 0036) | `251c74c06` 2026-10-02 | 0026, 0027 |
| [0035](0035-application-config-into-bootstrap.md) | application *ApplicationConfig를 지우고 스캔 설정을 각 앱 부트스트랩의 중첩 설정으로 옮긴다 | 채택 | `deae9a8e0` 2026-10-03 | — |
| [0036](0036-app-marker-removal.md) | 앱 마커 애노테이션을 제거하고 앱 소속을 Gradle 모듈(core + {앱}-application 4개)로 표현한다 | 채택(PG SPI 포트·라우터 배치 → 0061) | `d4688a682` 2026-10-04 | 0013, 0014, 0034 |
| [0037](0037-infrastructure-package-root.md) | infrastructure 모듈의 자바 패키지 루트를 com.tastyhouse.infrastructure.{모듈}로 통일한다 | 채택 | `e03a0df33` 2026-10-04 | — |
| [0038](0038-autoconfiguration-imports-removal.md) | AutoConfiguration.imports를 제거하고 앱 ModuleScanConfig 문자열 스캔으로 조립한다 | 채택 | `a400ec42e` 2026-10-04 | 0017 |
| [0039](0039-querydsl-tuple-removal.md) | 조회 DAO에서 QueryDSL Tuple을 전면 제거하고 public Row record로 투영한다 | 채택 | `a373e0a4d` 2026-10-04 | — |
| [0040](0040-package-private-visibility.md) | 내부 구현 클래스를 package-private으로 좁힌다(벤더 wire DTO는 루트 평면 배치) | 채택 | `151eadb88` 2026-10-04 | — |
| [0041](0041-domain-service-into-model.md) | domain의 service 패키지를 model로 흡수한다 | 채택 | `b4f1dc866` 2026-10-04 | 0027(일부) |
| [0042](0042-error-code-module-split.md) | 에러코드·예외를 그것을 던지는 가장 안쪽 모듈이 소유하도록 분할한다 | 채택 | `b188384ee` 2026-10-05 | 0025(ErrorContracts 부분) |
| [0043](0043-usecase-per-service.md) | CQRS 쌍 서비스를 유스케이스당 서비스 1개로 분리한다 | 채택 | `d0b32cfe1` 2026-10-08 | — |
| [0044](0044-jparepository-method-ban.md) | JpaRepository에 메서드를 선언하지 않고 QueryDSL 어댑터로 옮긴다 | 채택 | `65da13f9a` 2026-10-08 | — |
| [0045](0045-banner-mybatis-provider-pilot.md) | banner 쓰기를 JPA·MyBatis 별도 모듈로 구현하고 provider 키로 하나만 등록한다 | 대체됨(→ 0052) | `79cebac26` 2026-10-09 | — |
| [0046](0046-write-port-load-save-split.md) | 쓰기 포트를 LoadPort·SavePort로 목적별 분리한다 | 채택 | `085fa5559` 2026-10-09 | 0032(일부) |
| [0047](0047-jpa-module-split.md) | JPA·QueryDSL 코드를 infrastructure:jpa로 옮기고 persistence를 코드 없는 조립 모듈로 만든다 | 채택 | `4178929c3` 2026-10-09 | — |
| [0048](0048-async-config-core.md) | AsyncConfig(@EnableAsync)를 앱이 아니라 코어 application이 소유한다 | 채택 | `456db1b13` 2026-10-09 | — |
| [0049](0049-verification-context-rename.md) | 인증 컨텍스트 패키지를 phoneverification·emailverification으로 바꾸고 타입·식별자 어휘(mail/sms)는 유지한다 | 채택 | `456db1b13` 2026-10-09 | — |
| [0050](0050-social-login-router.md) | 소셜 로그인 벤더를 SocialOAuthClientRouter로 묶고 서비스 4벌을 SocialLoginService 하나로 합친다 | 채택 | `456db1b13` 2026-10-09 | — |
| [0051](0051-mysql-module-split.md) | MySQL 드라이버·datasource·커넥션 풀을 infrastructure:mysql로 옮긴다 | 채택 | `34edab8d2` 2026-10-09 | 0047(드라이버 소유 부분) |
| [0052](0052-banner-write-primary.md) | 같은 포트를 여러 기술이 구현하면 둘 다 등록하고 쓰는 쪽에 @Primary를 단다(provider 키 삭제) | 채택 | `f91ff6659` 2026-10-09 | 0045 |
| [0053](0053-notice-mybatis-legacy.md) | mybatis 모듈의 banner 파일럿을 지우고 notice 레거시 MyBatis 구현(쓰기+조회)을 둔다 | 채택 | `8ea00a525` 2026-10-10 | 0045 |
| [0054](0054-collaborator-service-package-private.md) | ..service..의 협력 서비스도 package-private으로 좁히고 다른 패키지가 쓰는 30개만 허용 목록에 둔다 | 채택 | `58774e4a9` 2026-10-11 | 0040(후속 과제) |
| [0055](0055-payment-refund-pessimistic-lock.md) | 환불 요청 누적액 검사는 PAYMENT 행 비관적 락으로 직렬화한다 | 채택 | `03d806112` 2026-10-15 | — |
| [0056](0056-error-catalog-additive-snapshot.md) | 신규 에러코드는 스냅샷 tsv에 행을 추가하고 개수 단정을 올린다 | 채택 | `03d806112` 2026-10-15 | — |
| [0057](0057-anchor-floor-on-intentional-deletion.md) | 개수 anchor 하한은 유스케이스를 의도적으로 삭제할 때만 실측값으로 내린다 | 채택 | `0068b7eae` 2026-10-16 | — |
| [0058](0058-point-balance-pessimistic-lock.md) | 포인트 잔액 변경은 POINT 행 비관적 락으로 직렬화한다 | 채택 | `18112f232` 2026-10-17 | — |
| [0059](0059-payment-confirm-race-lock.md) | PG 승인·실패 반영은 결제 행을 잠그고, PG 승인 성공을 사실로 삼는다 | 채택(부분 → 0060) | `fff56ac2b` 2026-10-18 | — |
| [0060](0060-pg-confirm-unknown-lookup.md) | PG 승인 결과 불명은 실패가 아니라 대기로 두고, 즉시 조회로 대사한다 | 채택(부분 → 0063) | `fff56ac2b` 2026-10-18 | 0059(통신 오류 FAILED·"이미 처리됨" 400 부분) |
| [0061](0061-pg-ports-to-core-batch-reconcile.md) | PG 포트·라우터를 코어로 옮기고, batch가 미확정 결제를 주기적으로 대사한다 | 채택(부분 → 0062) | `fff56ac2b` 2026-10-18 | 0036(PG SPI를 web-application에 두던 부분) |
| [0062](0062-payment-reconcile-backoff.md) | batch 미확정 결제 대사에 결제별 재시도 백오프를 둔다 | 채택(부분 → 0064) | `fff56ac2b` 2026-10-18 | 0061(같은 결제를 매 주기 다시 고르고 반복 ERROR를 감수하던 부분) |
| [0063](0063-pg-confirm-unreadable-4xx-unknown.md) | 본문을 해석할 수 없는 PG 승인 4xx는 거절이 아니라 결과 불명으로 분류한다 | 채택(부분 → 0065) | `fff56ac2b` 2026-10-18 | 0060(결과 불명을 5xx·예외·빈 응답으로 한정하던 부분) |
| [0064](0064-payment-reconcile-claim.md) | batch 대사는 조건부 UPDATE로 결제별 차례를 선점한 뒤에만 PG에 묻는다 | 채택 | `fff56ac2b` 2026-10-18 | 0062(일정 기록 실패 시 조회 계속·횟수 비원자화 부분) |
| [0065](0065-pg-cancel-lookup-recovery.md) | PG 취소의 "이미 취소됨"·결과 불명은 조회로 확정하고, 취소 실패 원인을 분류한다 | 채택 | `93aa93994` 2026-10-27 | 0063(`PG_API_ERROR`를 취소 전용으로 남기던 부분) |

## 원문 표기 → ADR 대응표

정리 전 규칙 문서에 있던 이력 꼬리표로 검색하던 사람을 위한 대응표입니다.

### 이력 꼬리표 → ADR

| 횟수 | 꼬리 변형 (`번복됨 — X`의 X, 40자 절단) | ADR |
|---|---|---|
| 69 | imports 제거 | 0038 |
| 59 | persistence domain 재허용 | 0031 |
| 53 | 앱 마커 제거 | 0036 |
| 43 | application ' | 0034 (원문 `application *ServiceConfig 삭제`) |
| 36 | 에러코드 모듈 분할 | 0042 |
| 34 | infrastructure 패키지 루트 통일 | 0037 |
| 30 | notice-mybatis-legacy | 0053 |
| 24 | social-login-router | 0050 |
| 22 | banner-write-primary | 0052 |
| 18 | 덩어리 03b | 0028 |
| 14 | package-private 적용 | 0040 |
| 9 | 03a | 0027 |
| 7 | jpa 모듈 분리 | 0047 |
| 6 | 덩어리 01 | 0025 |
| 5 | 덩어리 03a | 0027 |
| 5 | domain service→model 흡수 | 0041 |
| 4 | 03b | 0028 |
| 4 | imports 제거로 삭제 | 0038 |
| 3 | 덩어리 02/03a | 0026 (+0027) |
| 3 | 02-vendor-ports | 0026 |
| 3 | JpaRepository 메서드 선언 금지 | 0044 |
| 3 | 삭제 | 0034 (application-core 대상 경로 3건) |
| 3 | QueryDSL Tuple 전면 제거 | 0039 |
| 2 | backend 덩어리 02-vendor-ports | 0026 |
| 2 | 협력 서비스 후속 적용 | 0054 |
| 2 | '@SuppressWarnings' 지양 규칙 | 0029 |
| 2 | 아웃바운드 포트·어댑터 네이밍 전환 | 0032 |
| 2 | chunk 02-vendor-ports | 0026 |
| 2 | 과거엔 DomainConfig를 가진 채널 모듈이었다 | 0026 |
| 1 | 계약이 security-core로 | 0023 |
| 1 | 정책 B 이후 ' | 0004 (+0028) |
| 1 | 02-vendor-ports로 세 채널 모듈의 코드가 전부 사라졌고 | 0026 (+0037) |
| 1 | 02-vendor-ports 이전엔 'com | 0026 |
| 1 | 표식 제거 | 0036 |
| 1 | 나머지 ' | 0034 |
| 1 | 표현 계층 domain 절단 덩어리 01 | 0025 |
| 1 | 위 현행 표기로 읽는다 | 0031 |
| 1 | java 주석 금지 | 0020 |
| 1 | async-config-core | 0048 |
| 1 | verification-context-rename | 0049 |
| 1 | 아래 [security-core로 옮긴 이유](#ratelimitcoun | 0023 |
| 1 | 지금 방향은 'infrastructure | 0023 |
| 1 | 계약도 이 모듈을 떠났다 | 0023 |
| 1 | 지금은 순환이 아니다 | 0023 |
| 1 | 이 판단은 이제 persistence 'ReviewBlindRequest | 0031 |
| 1 | ServiceConfig 삭제 | 0034 |
| 1 | 이벤트를 받아 도메인 서비스를 오케스트레이션하는 것은 유스케이스 계층의  | 0024 |
| 1 | 위 갱신 | 0044 |
| 1 | 서비스 간 구체 주입 제거 | 0033 |
| 1 | 엄격 레이어드 01·02·03b | 0028 (+0025, 0026) |
| 1 | 정책 B·03b | 0004 (+0028) |
| 1 | 'application-core/AGENTS | 0029 |
| 1 | mysql 모듈 분리 | 0051 |
| 1 | 과거엔 라우터 등록 코드를 가진 채널 모듈이었다 | 0026 |
| 1 | 'RateLimitCounterPort'도 'security-core'로 | 0023 |
| 1 | 'afterName' 검증은 'api-common-module'로 옮겨졌 | 0023 |
| 1 | '@SuppressWarnings' 지양 규칙으로 배선 | 0029 |
| 1 | 덩어리 01에서 'domain' 의존 삭제 | 0025 |
| 1 | 설정 클래스 삭제 | 0038 |
| 1 | 빈 존재 조건 삭제 | 0038 |

총 60종, 미대응 0.

(표 안의 백틱은 표 깨짐을 막으려고 `'`로 바꿨다. 원문은 `tails.txt`.)

### 꼬리표 없는 이력 문구 → ADR

| 원문 문구(대표형) | 출현 파일 | ADR |
|---|---|---|
| 제목 "인바운드 포트(UseCase 인터페이스)를 도입한다 — 완전 매핑 전략 채택 (과거 결정의 명시적 번복)" / "그 결정을 번복한다" | backend/CLAUDE.md | 0005 |
| "Request → Command 변환은 … (개정 — 과거 `toCommand` 금지 규칙의 번복)" | backend/CLAUDE.md | 0006 |
| "application 모듈이 읽기 계약을 보는 경로 (개정 — 과거 'infra를 컴파일 타임에 본다'는 서술의 번복)" / "(개정됨 — 읽기 경로 포트화)" / "모듈 경계 (개정됨 — 읽기 경로 포트화로 재개정)" | backend/AGENTS.md, backend/CLAUDE.md, jpa | 0007 |
| "관리자 측 application 계층은 `application`이 소유한다 (챕터 03 개정 — 과거 '이 모듈이 관리자 측 application 계층이다'의 번복)" / ceo-api 동형 / "(챕터 05 개정 — 2차원 경계)" / "(개정: 소유 모듈은 `{앱}-application`)" | admin-api, ceo-api, backend/AGENTS.md, batch-module | 0008 |
| 제목 "다중 앱 공유 계약은 `domain`이 소유한다 — **번복됨** (모듈 재편 챕터 05 → 의존성 정리 챕터 04)" / "**왜 번복했나.**" | backend/CLAUDE.md | 0009(이전)·0015(대체) |
| "> **번복됨 (표현 계층 domain 절단 덩어리 01)**" / "아웃바운드 강등 방향은 번복됐습니다" / "**아래는 번복된 과거 내용**" | backend/AGENTS.md, backend/CLAUDE.md | 0010(이전)·0025(대체) |
| "**이전 판단 — 표현 계층에 둔다 (번복됨)**" / "~~방향은 이제 `redis → api-common`이다.~~ **(번복됨)**" / "~~redis 모듈에 둔다~~ 번복됨" | api-common, redis | 0011(이전)·0023(대체) |
| "도메인/DTO → 응답 변환 (개정 — … '원시타입 낱개 언패킹' 규칙의 명시적 번복)" | backend/CLAUDE.md | 0012 |
| 제목 "과거 판단의 번복 — 앱 축을 접은 이유 (챕터 01)" / 제목 "과거 '앱별 모듈 4개 → 1개' 통합의 번복 근거" / "다시 번복됐습니다" / "앱 마커 제거로 번복" | application-core, backend/CLAUDE.md, CLAUDE.md, backend/AGENTS.md | 0013(이전)·0036(대체) |
| 제목 "챕터 03 — 패키지 평탄화 + 앱 마커 애노테이션 (과거 판단의 번복)" / 제목 "앱 마커 규칙 (챕터 03 — …)" / "빈 배선 (챕터 03 개정 — …)" / "(개정 — 챕터 03 이후 예외 범위 확대)" / "과거 마커 애노테이션은 번복됨" | application-core, backend/CLAUDE.md, jpa | 0014(이전)·0036(대체) |
| "비채택 대안 (1)/(3) … → 채택으로 번복 (external 분리)" / restclient "명시적으로 번복한 것이다" | backend/CLAUDE.md, restclient | 0016 |
| "→ 이후 3분할(2026-09-26)로 재번복" / "번복 근거는 설정 공유가 아니라 의존 무게였다" | backend/CLAUDE.md | 0022 |
| "(**챕터 02로 `runtimeOnly`로 강등 — 과거 서술의 번복**)" / "(챕터 02 개정, 전면 교체)" / "(챕터 02 개정 — auto-configuration)" / "(챕터 02 신설 → 챕터 01 개정)" | web/admin/ceo-api, api-common, batch-module | 0017(이전)·0038(대체) |
| 루트 모듈 수 연대기(17→14→20→21→23→24→27→28→32→36→37→38→39), "(모듈 수 24는 불변 …)", "`infrastructure:external` → `infrastructure:http-client`를 거쳐 리네임" | CLAUDE.md, backend/CLAUDE.md, backend/AGENTS.md(settings.gradle 행) | 0008·0009·0013·0016·0018·0022·0036·0045·0047·0051 (값마다 해당 ADR) |
| "> **번복 기록 (챕터 01)**: 이 절은 원래 '토큰 저장소 6종은 `security-module`에 잔류한다'였다" / "이 번복에도 바이트 단위로 불변" / "(챕터 01에서 끊김)" / "(~~사례~~ 챕터 01에서 소멸)" | redis, backend/CLAUDE.md | 0019 |
| 제목 "과거 판단의 번복 — 파일 저장 SPI 삭제" / "과거 판단의 번복" 앵커 링크 | restclient, file-storage | 0021 |
| "(번복됨) 이벤트 리스너를 이 모듈의 `<ctx>/listener/`에 두지 않는다" / "과거 infrastructure-module 배치는 번복됨" / "과거 위치 `infrastructure:persistence`는 번복됨" | jpa, domain, domain/src/… | 0024 |
| "번복됨 (덩어리 01)" / "번복됨(덩어리 01)" / "(번복 — 덩어리 01, 과거 `domain`(api))" / "(번복) `@SharedApp` 허용 대상 확대" / "(번복) 표현 계층은 `BusinessException`을 타입으로 잡지 않고 …" / "'리스너 외에는 달지 않는다'는 번복됨(표현 계층 domain 절단 프로그램 덩어리 01)" | backend/AGENTS.md, backend/CLAUDE.md, api 3모듈, api-common, application-core, security-core, security-module | 0025 |
| 제목 "이 모듈에 코드를 넣지 않는다 (번복됨 — 과거엔 …)" 3건 / "'채널 모듈이 벤더를 조립하는 쪽이라 DomainConfig를 갖는다'던 과거 규칙 … 번복됐다" / "**출력 포트 — 파일·메일·SMS·결제는 이 모듈에 없다 (번복됨)**" | mail, sms, pg, domain/src/… | 0026 |
| "**번복됨 — 03a.**" (순환 봉인 해제) | domain | 0027 |
| 제목 "(번복됨 — persistence domain 재허용) 덩어리 03b — …" + 하위 "(번복됨)" 제목 3개 / "(03b 동안은 `XxxStatePortImpl` — 번복됨)" 류 / "(03b 번복 — …)" / "이 판단은 번복됐다"(GeoPolygonTextCodec) | application-core, jpa, domain | 0028(이전)·0031(대체) |
| "~~`XxxCodes` …~~ **(번복됨)**" 계열 / "~~마스킹이 실사용에 연결되지 않은 …~~ **(번복됨)**" | backend/CLAUDE.md, application-core, jpa, logging-module | 0029 |
| "`domain.exception` … 지금은 접두어가 `domain`이면 전부 1순위입니다(**번복**)" | backend/CLAUDE.md | 0030 |
| 제목 "persistence domain 재허용 — State 계열 제거 (03b 쓰기 경로 번복)" / "**번복 근거 — 애그리거트 하나에 파일 5개**" / "(번복 후: …)" / "(개정 — persistence domain 재허용, …)" / "쓰기 어댑터에 1순위가 돌아왔다. 번복 근거는 …" | application-core, backend/CLAUDE.md, jpa | 0031 |
| "서비스 간 협력도 UseCase 인터페이스로 주입한다 (신설 — 과거 '협력 서비스는 구체 클래스를 주입한다' 규칙의 번복)" / "절 번복 표기" | backend/CLAUDE.md, application-core | 0033 |
| 제목 "application `*ServiceConfig` 전면 삭제 — … (02/03a 등록 방식 번복)" / "(번복) application `*ServiceConfig` 전면 삭제" / "'마커 없는 POJO를 `@Bean`으로만 등록'은 번복됨" / "**왜 '현재 소비자' 기준인가 (번복 — 과거 …)**" | application-core, backend/CLAUDE.md | 0034 |
| "(과거 표현: … — `*ApplicationConfig` 삭제로 번복됨.)" / "아래 번복된 절의 '중첩 클래스여야 하는 이유' 항목은 지금도 유효하다" | backend/CLAUDE.md | 0035 |
| 제목 "앱 마커 제거 — 앱 모듈 재분리 (챕터 01 통합·챕터 03 마커 번복)" / "과거 컨벤션 '마커만 = 도메인 서비스 …'는 번복됐다" / "앱 마커 규칙 링크 — 지금은 번복됨" / "… `AppIsolationTest#appRestrictedPortDependentsShouldBelongToThatApp`이 잡는다'는 번복됐다" | application-core, backend/CLAUDE.md, mail, sms | 0036 |
| "**과거 — auto-configuration 인벤토리 (번복됨)**" / "— 번복됨, imports 제거" 괄호(admin/ceo/web-api·firebase·oauth·file-storage·security-module·application-core) / "(imports 제거 후 개정)" / "(imports 제거로 재개정)" / "— 번복됨, 설정 클래스 삭제" | backend/CLAUDE.md 외 다수 | 0038 |
| "**(번복)** 과거 `infrastructure/jpa/AGENTS.md`는 2컬럼 key→value 수집용 Tuple을 의도적으로 남겼으나 …" | backend/CLAUDE.md | 0039 |
| "wire DTO — package-private (번복됨: 과거 dto/ 하위)" | bbq | 0040 |
| "**번복됨 — domain service→model 흡수**"(괄호 없는 표기) | application-core, domain | 0041 |
| "'새 예외 타입을 만들지 않습니다' 항목은 … **번복**됐다" / 제목 "과거 규칙과 그 위치 (번복 표기 외에는 유효)" / "— 번복됨, 에러코드 모듈 분할" | backend/CLAUDE.md, bbq | 0042 |
| "(번복 완료 — 유스케이스당 서비스 1개)" / "(번복 진행 중 — …)" / "**번복 — 과거 '도메인당 CQRS 분리 …'**" / "상단 번복 표" / "입도 규칙 (번복 완료)" | backend/CLAUDE.md, backend/AGENTS.md, web/admin/ceo-api, webapi 하위, admin-application, application-core | 0043 |
| 제목 "채널 도메인 어휘 통일 규칙 (… 패키지는 번복됨)" / "패키지 어휘에 관한 부분만 번복이고" | backend/CLAUDE.md | 0049 |
| "(jpa 모듈 분리) 이 문서의 `infrastructure:persistence` 읽는 법" / "… `infrastructure:jpa`로 읽는다"(전역 면책) | backend/CLAUDE.md, jpa | 0047 |
| "~~모듈별로 각각 둠~~ → `api-common-module`이 단독 소유 (개정됨)" / "(과거 '모듈별로 각각 둠' 관례 개정)" | backend/CLAUDE.md, backend/AGENTS.md, api-common | 0003 |
| "변환 방법 (개정됨 — query DAO가 URL을 완성해 투영)" | backend/CLAUDE.md | 0002 |
| "공유/비공유 `@Embeddable` VO 경계 (개정됨)" / "scanBasePackages에서 domain 스캔 엔트리를 제거한다 (개정됨)" / "(과거 '공유 VO는 core에 어노테이션을 유지한다'는 규칙 … 역전되었다)" | backend/CLAUDE.md | 0001 (+0028) |


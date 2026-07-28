# banner 도메인 전환

> 선행: `tasks/README.md` + `00-phase0`. 그룹 1(병렬 가능).

## 현황
- core: `banner/application/` — `BannerCommandService`, `BannerQueryService` + result. 단일 애그리거트 `Banner`(enum `BannerType`). 조회가 `QUploadedFileJpaEntity`(infra)와 조인.
- 소비자: admin-api(CRUD), web-api(노출 배너 조회).

## 작업
1. **분류**: command 전량 (A), read (B). 이벤트·포트 없음.
2. **(B)**: infra `infrastructure/banner/query/BannerQueryDao` 신설(패턴 3 — admin/web 소비자별 메서드 분리). file 조인 투영(`QUploadedFileJpaEntity`)은 같은 모듈 내 참조가 되어 자연스럽게 해결. Result·SearchCondition은 infra query 패키지 소유(충돌 시 `Management` 한정어). admin/web 각 `BannerQueryService`(readOnly)가 DAO 주입. `BannerRepository` write 순수화, infra `BannerRepositoryImpl` 축소(패턴 4).
3. **(A)**: admin에 `BannerCommandService`(@Transactional) 신설해 core `BannerCommandService` 흡수(패턴 2 — 기존 facade `BannerService`는 Command/Query로 분해). `BannerCreateCommand`/`BannerUpdateCommand` 삭제. `BannerType.from(String)` 승격 규칙은 CommandService에 유지.
4. core `banner/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.

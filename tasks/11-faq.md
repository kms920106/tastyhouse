# faq 도메인 전환

> 선행: `tasks/README.md` + `00-phase0`. 그룹 1(병렬 가능).

## 현황
- core: `faq/application/` — `FaqCommandService`(2 repo: Faq+FaqCategory), `FaqCategoryCommandService`, QueryService + result(`FaqListItemResult`, `FaqCategoryResult`, `FaqCategoryManagementResult` 등).
- 소비자: admin-api(CRUD), web-api(조회).

## 작업
1. **(C) 검토**: `FaqCommandService`가 2 repo 주입 — 실제로 한 메서드에서 Faq와 FaqCategory를 **함께 save**하는지 확인. 카테고리 존재 검증만이면 (C) 아님 → (A)로 처리. 카테고리 삭제 시 "활성 Faq 존재하면 삭제 금지"(`existsActiveItemsByCategoryId`) 같은 크로스 애그리거트 **규칙**은 도메인 서비스 `FaqCategoryDeletionPolicy`(가칭)로 하강 후 admin 서비스가 호출.
2. **(B)**: infra `infrastructure/faq/query/FaqQueryDao` 신설(패턴 3 — 도메인당 1개, admin/web 소비자별 메서드 분리), 소비 모듈이 실제 쓰는 result만 infra query 패키지로 이관. `Management` 한정어(`FaqCategoryManagementResult` 등)는 **유지·부여**한다 — admin/web result가 이제 같은 패키지에 공존하므로 충돌은 사라지지 않는다(README 패턴 3, 과거 "제거 가능" 문구 폐지). admin/web 각 `FaqQueryService`(@Transactional(readOnly = true))가 DAO를 주입해 Response 조립. `FaqRepository`/`FaqCategoryRepository` write 순수화(패턴 4).
3. **(A)**: admin에 `FaqCommandService`·`FaqCategoryCommandService`(@Transactional) 신설해 core command 로직 흡수(패턴 2 — 기존 facade Service는 Command/Query로 분해). Command DTO 삭제.
4. core `faq/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.

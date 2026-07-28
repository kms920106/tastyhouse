# event(이벤트/당첨자) 도메인 전환

> 선행: `tasks/README.md` + `00-phase0` + 그룹 1 완료. 그룹 2(병렬 가능).

## 현황
- core: `event/application/` — `EventCommandService`(3 repo: Event+EventWinner+EventAnnouncement), QueryService + result(`EventManagementListItemResult` 등). 조회가 `QUploadedFileJpaEntity` 조인.
- 소비자: admin-api(이벤트 CRUD·당첨자·발표), web-api(이벤트 목록/상세).

## 작업
1. **(C) 판정**: 3 repo 주입이지만 메서드별로 단일 애그리거트 연산(Event CRUD / Winner 등록·삭제 / Announcement 수정)일 가능성 높음 — 메서드 단위로 확인해 실제 원자 다중-save만 도메인 서비스로 하강, 나머지는 (A).
2. **(A)**: admin `EventCommandService`(@Transactional)로 command 흡수(패턴 2 — 기존 facade `EventService`는 Command/Query로 분해, 명시적 save 유지). `deleteWinner` 평탄화 경로 등 기존 컨트롤러 계약 무변경.
3. **(B)**: infra `infrastructure/event/query/EventQueryDao` 신설(패턴 3 — admin용 관리 목록/상세, web용 노출 목록/상세 메서드 분리). `EventManagementListItemResult` 등 `Management` 한정어는 **유지·부여**한다 — admin/web result가 같은 패키지에 공존하므로 충돌은 사라지지 않는다(과거 "제거 가능" 문구 폐지). file 조인(`QUploadedFileJpaEntity`)은 같은 모듈 내 참조. admin/web 각 `EventQueryService`(readOnly)가 DAO 주입. Repository 3개 write 순수화(패턴 4).
4. `PhoneNumber` VO(EventWinner) 접근자 규칙 유지.
5. core `event/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.

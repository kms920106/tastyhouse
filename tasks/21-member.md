# member(+follow/referral) 도메인 전환

> 선행: `tasks/README.md` + `00-phase0` + 그룹 1 완료. 그룹 2(병렬 가능). **이벤트 리스너 2개·다수 소비자 주의.**

## 현황
- core: `member/application/` — `MemberCommandService`(3 repo, 이벤트 발행), `MemberEventListener`, `member/referral/application/ReferralCommandService`(이벤트 발행)·`ReferralRegisteredEventListener`, follow의 command/query, QueryService·result 다수.
- 소비자: web-api(프로필·팔로우·추천인·소셜로그인 4종 서비스), admin-api(회원 관리·정지/활성), **external-api**(`MemberCommandService` import 확인됨 — 사용 지점 조사), security(UserDetails 조회 여부 확인).

## 작업
1. **(C) 하강**: `MemberCommandService` 중 3 repo가 얽히는 연산(탈퇴: Member+SocialAccount+Withdrawal 원자 처리 등)을 `MemberWithdrawalService`(가칭) 등 도메인 서비스로 하강. referral의 `register`(등록+reward 재할당 흐름, save 재할당 회귀 주의 — AGENTS.md 선례)도 도메인 서비스로.
2. **(E)**: `MemberEventListener`·`ReferralRegisteredEventListener` → infrastructure `member/listener/`. 본문은 도메인 서비스 호출로 축소, 발행측은 `DomainEventPublisher`로 교체.
3. **(A)**: 프로필/비번 변경·suspend/activate 등 단일 애그리거트 연산은 web/admin 각자의 `MemberCommandService`(@Transactional)로 흡수(패턴 2 — 기존 facade `MemberService`는 Command/Query로 분해, 명시적 save 유지). 소셜로그인 4종 서비스(web)의 `updateProviderInfo`+`saveSocialAccount` 흐름은 web application에 유지하되 core command 의존 제거.
4. **(B)**: infra `infrastructure/member/query/MemberQueryDao` 신설(패턴 3 — web용 내 정보·팔로우 목록, admin용 회원 목록 `MemberListItemResult`·검색 메서드 분리, follow/referral 조회 포함). Result·Condition은 infra query 소유(충돌 시 `Management` 한정어). web/admin 각 `MemberQueryService`(readOnly)가 DAO 주입. `PhoneNumber` VO record 접근자(`value()`) 규칙 유지.
5. external-api·security의 core member application import 지점을 domain repository/도메인 서비스 직접 사용으로 교체.
6. core `member/**/application/` 삭제(follow/referral 포함).

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.

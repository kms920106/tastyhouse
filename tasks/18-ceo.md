# ceo(점주 계정) 도메인 전환

> 선행: `tasks/README.md` + `00-phase0`. 그룹 1(병렬 가능).

## 현황
- core: `core/domain/ceo/` — 점주 계정 애그리거트. 작업 전 `ls core-module/.../core/domain/ceo/application`으로 서비스·DTO 인벤토리 직접 확인(이 파일 작성 시점에 상세 미조사).
- 소비자: ceo-api(인증·계정), admin-api(점주 배정·관리 가능성 — `grep -r "core.domain.ceo.application" admin-api/src`로 확인).

## 작업
1. 인벤토리 후 README 분류표 적용: 단일 애그리거트 command는 (A)로 소비 모듈 `{도메인}CommandService`에 흡수(패턴 2), read는 (B) infra `infrastructure/ceo/query/CeoQueryDao`(패턴 3), 다중 애그리거트 원자 연산만 (C) 도메인 서비스 하강.
2. security-module이 ceo application을 import하면(로그인 UserDetails 조회 등) 해당 지점은 domain repository 직접 사용으로 교체.
3. core `ceo/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.

# admin(관리자 계정) 도메인 전환

> 선행: `tasks/README.md` + `00-phase0`. 그룹 1(병렬 가능).

## 현황
- core: `admin/application/` — `AdminCommandService`(create), `AdminQueryService`. 단일 애그리거트 `Admin`(insert 전용, update 경로 없음).
- 소비자: admin-api 단독(`AdminAccountService`, 로그인·인증은 security 경유).

## 작업
1. **분류**: 전량 (A)+(B). 여기서 `Admin`은 역할 마커가 아닌 애그리거트 본명 — 리네이밍 대상 아님.
2. **(A)**: admin-api `AdminAccountService`를 패턴 2로 분해 — command(create)는 `@Transactional` CommandService 측에 core `AdminCommandService#createAdmin`을 흡수. `AdminCreateCommand` 삭제(원시 파라미터 수신). 비밀번호 인코딩 등 기존 처리 위치 유지.
3. **(B)**: read를 README "write 포트 잔류 판정 기준"으로 분류 — result DTO 반환(목록 등)은 infra `infrastructure/admin/query/AdminQueryDao`로 이관, username 단건 조회처럼 **인증·불변식에 필요한 엔티티 반환**은 `AdminRepository`(write 포트) 잔류.
4. security-module이 core admin application을 import하는지 확인(`grep -r "core.domain.admin.application" security-module/src`) — import 시 해당 지점은 domain repository 직접 사용으로 교체.
5. core `admin/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0, 추천 커밋 메시지 제시.

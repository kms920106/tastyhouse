# file 도메인 전환 (허브 — 전 도메인 완료 후 최후)

> 선행: **그룹 1~4 전부 완료 필수.** file은 전 도메인이 소비하는 허브이므로 마지막에 전환한다.

## 현황
- core: `file/application/` — `FileCommandService`(이벤트 발행), `FileUploadedEventListener`, `FileQueryService`(`findFilePath` 등 — 응답 URL 변환 규칙에서 전 모듈 사용), **출력 포트 `application/port/out/FileStoragePort`** — external-api(S3/Firebase)가 구현.
- 소비자: **전부** — web/admin/ceo(업로드·URL 변환), external-api(포트 구현 + `FileCommandService`/`UploadFileCommand` import 확인됨), batch.

## 작업
1. **포트 이동**: `application/port/out/FileStoragePort` → `domain/file/port/`. external-api 어댑터 import 갱신.
2. **(C) 판정**: 업로드(스토리지 저장 + UploadedFile 메타 save + 이벤트 발행)는 액터 무관 연산 → `FileUploadService`(가칭) 도메인 서비스로 하강(FileStoragePort·DomainEventPublisher 주입). external-api의 `FileCommandService` 직접 import 지점도 이것으로 교체.
3. **(E)**: `FileUploadedEventListener` → infrastructure `file/listener/`.
4. **(B)**: `findFilePath`/`findById`는 사실상 도메인 조회(URL 변환 재료) — README "write 포트 잔류 판정 기준" 적용: result DTO가 아닌 엔티티/값 반환이면 `UploadedFileRepository`(domain write 포트)로 통합하고 QueryService 삭제(infra query DAO를 만들지 않는다 — 표현 목적 read model이 없음). 각 모듈 Service의 `fileQueryService.findFilePath(...)` 호출부(URL 변환 private 매퍼 — 다수)를 repository 직접 호출로 일괄 교체.
5. `UploadFileCommand` 등 command DTO 정리(도메인 서비스 입력 record로 격하 또는 삭제).
6. core `file/application/` 삭제.

## 완료 기준
- 전 모듈 LSP 오류 0 + **core-module 전체에 `application` 패키지가 하나도 남지 않음**을 확인(`find core-module/src/main -path "*application*" | wc -l` → 0). 추천 커밋 메시지 제시.

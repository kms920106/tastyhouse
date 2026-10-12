# ADR-0002: 파일 경로→URL 변환은 Service가 아니라 query DAO가 투영 시점에 끝낸다

- 상태: 채택
- 결정 커밋: `70eb33376` (2026-08-02)
- 대체한 결정: —

## 맥락

DB에는 파일의 저장 경로(`UPLOADED_FILE.file_path`)만 저장하고, 화면에 보여줄 URL은 서버가 만든다. Firebase Storage는 경로를 인코딩하고 `?alt=media`를 붙여야 하므로 프론트엔드가 URL을 직접 조립할 수 없다.

- **query DAO**: 화면 표시용 조회(QueryDSL)를 담당하는 infrastructure 클래스. 지금 이름은 `{Ctx}QueryAdapter`.
- **투영(projection)**: 조회 결과 행을 Result record로 만드는 것.

문제는 변환을 어디서 하느냐였다. 커밋 당시 상황(커밋 메시지·`backend/CLAUDE.md` 원문):

- 같은 변환(`fileService.getUrlByPath(dto.xxxFilePath())`)이 api 모듈 Service 60여 곳에 흩어져 있었다.
- 그 결과 세 모듈의 `FileService`가 서로 다르게 갈라졌다(web엔 배치 변환이 없고, ceo엔 경로 변환이 없고, `findFileResponse`는 admin에만 있었다).
- Result가 `Long fileId`만 담던 11개 경로는 응답을 조립하면서 파일을 다시 조회해 DB 왕복이 늘었다.

## 이전 규칙

### `backend/CLAUDE.md` — 응답 record 파일/이미지 필드 URL 규칙

응답 record는 `~FileId` 대신 `~Url`을 노출한다는 규칙은 같았지만, filePath·fileId → url 변환은 **api 모듈 Service**가 했다. Service에 `fileService.getUrlByPath(...)`·`getUrlByFileId(...)`·`getUrlsByFileIds(...)`·`findFileResponse(...)` 같은 변환 매퍼가 있었고, `UploadedFileRepository`에 표현용 조회(`findFilePath`·`findFilePaths`)가, domain `FileUploadService`에 `getUrlByPath`가 있었다.

## 결정

| 항목 | before | after |
|---|---|---|
| 변환 주체 | api 모듈 Service | infrastructure query DAO(조회 시점) |
| Result record가 담는 값 | `~FilePath` 또는 `~FileId` | 완성된 `~Url`(String) |
| 변환기 | 모듈별 `FileService` 메서드 4종 | `FileUrlResolver`(`@Component`) 하나 — 도메인 출력 포트 `FileStoragePort` 경유 |
| api `FileService` | 업로드 + URL 변환 | 업로드 전용(`upload(MultipartFile)`) |
| fileId만 있던 경로 | 응답 조립 중 파일 재조회 | DAO에 파일 join 추가 |
| write 포트의 표현용 조회 | `findFilePath`·`findFilePaths`·`FileUploadService.getUrlByPath` | 삭제 |

## 근거

- 표현 목적 read model을 화면이 필요로 하는 형태로 완성해 내려보내는 것은 CQRS read 측의 정상 책임이다(원문은 Microsoft Learn의 CQRS·Materialized View 가이드를 근거로 든다). 변환 지점을 read 어댑터 한 곳으로 모으면 드리프트가 사라진다.
- driven 어댑터(DAO)가 도메인 포트(`FileStoragePort`)를 쓰는 형태라 의존 방향이 안쪽을 유지한다.
- SQL `CONCAT`으로 URL을 만들지 않는다 — Firebase와 S3의 규칙이 다르고 `baseUrl`이 환경 설정값이라, SQL로 재현하면 인코딩이 깨지고 설정이 하드코딩된다.

## 결과·감수한 손실

- 변환 메서드 4개가 사라져 세 모듈 `FileService`가 동일한 파일이 됐고, fileId만 있던 11개 경로의 추가 쿼리가 사라졌다. 동작 변경은 없었다(event 상세는 left join 미스 시 기존 `FILE_NOT_FOUND` 예외를 보존).
- 커밋 당시에는 `@QueryProjection`이 생성자 직접 투영이라 fetch 직후 Result를 재조립하는 헬퍼(`withResolvedXxx`, 54개)를 썼다. 같은 타입 슬롯을 바꿔 넣어도 컴파일이 통과하는 구조였고, 이후 `d7ef7d002`(2026-09-23)가 투영식 인자 `fileUrlResolver.urlOf(경로컬럼)`로 바꿔 헬퍼를 0개로 봉인했다.
- 커밋 당시 `OrderProductResult.imageUrl`을 "주문 시점 URL 스냅샷"이라며 유일한 예외로 뒀으나 실제 저장값이 경로여서 전제가 틀렸고, 주문 상세가 호스트 없는 경로를 내려보내는 장애가 났다. 이후 `ORDER_PRODUCT.image_url` → `image_file_id`로 바꿔 예외를 없앴다.
- api 모듈 `FileService`는 이후 api-common-module 통합([ADR-0003](0003-api-common-shared-plumbing.md))을 거쳐 application 유스케이스로 내려가 소멸했다.

## 영향받은 문서

- `backend/CLAUDE.md` · "응답 record 파일/이미지 필드 URL 규칙 (`~FileId` 노출 금지, 표시용 URL만)" · 변환은 Service가 아니라 query DAO가 끝낸다
- `backend/CLAUDE.md` · "파일 URL 조립 위치 규칙 (query DAO가 `FileUrlResolver`로 완성)" · `urlOf` 투영, 재조립 헬퍼 0개 봉인, 예외 없음
- `backend/CLAUDE.md` · "query DAO·QueryPort·Result DTO·SearchCondition 소유 규칙 (읽기 계약은 전부 `application-core`가 소유, 구현은 `infrastructure:jpa`)" · DAO는 표현에 필요한 완성 형태로 투영한다

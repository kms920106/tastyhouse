# 작업지시서 05 — external-api FileService 책임 분리

## 배경 (왜)

이 프로젝트의 클린 아키텍처 원칙상 `external-api` 모듈은 `core-module`이 정의한 포트 인터페이스(예: `MailSender`, `SmsSender`, `PgPaymentGateway`)를 구현하는 **어댑터 모듈**이어야 한다. 실제로 email(`JavaMailEmailSender`/`AwsSesEmailSender`), sms(`AwsSnsSmsSender`/`SolapiSmsClient`), payment(`TossPaymentGatewayAdapter`)는 이 원칙을 잘 지키고 있다.

그런데 `file` 패키지의 `FileService`는 다르다. 이 클래스는 단순 어댑터가 아니라 **파일 검증 + 오케스트레이션 + `java.net.http.HttpClient`를 이용한 원격 이미지 다운로드**까지 수행하는 비대한 `@Service`다. 이는 "core에 유스케이스가 있고 external은 그 포트만 구현한다"는 방향에서 벗어나, **유스케이스 로직이 어댑터 모듈에 상주**하는 상태다. 게다가 `core-module`에는 이미 `file` 바운디드 컨텍스트(`domain/model`, `application/FileCommandService` 등)가 존재하므로, 논리적으로 이 유스케이스가 있어야 할 자리가 이미 마련되어 있다.

admin-api의 `file/` 도메인은 한술 더 떠서 **Service 계층 없이 컨트롤러가 external-api의 `FileService`를 직접 호출**한다 — 이는 "프레젠테이션은 core에 직접 의존하지 않고 Service/Facade를 통한다"는 계층 원칙에서도 벗어난 특수 사례다.

## 현재 상태 (근거)

- `external-api/src/main/java/com/tastyhouse/external/file/FileService.java` — 검증 로직 + 오케스트레이션 + `ByteArrayMultipartFile` 변환 + 원격 다운로드 로직 보유.
- `external-api/src/main/java/com/tastyhouse/external/file/FileStorageStrategy.java` — 포트 인터페이스(정상적인 어댑터 계약).
- `external-api/src/main/java/com/tastyhouse/external/file/s3/S3FileStorage.java`, `external-api/src/main/java/com/tastyhouse/external/file/firebase/FirebaseFileStorage.java` — `FileStorageStrategy` 구현체 2종(정상).
- `core-module`의 `file` 도메인: `domain/model/UploadedFile`(POJO, `of`/`reconstitute`), `application/FileCommandService` — 이미 존재.
- **호출부 규모**: web-api가 20개 이상 패키지에서, admin-api가 9개 패키지에서 `FileService`를 import한다. 가장 많이 소비되는 타입이 `FileService`(24회 import) — 즉 파일 업로드/조회를 쓰는 거의 모든 도메인이 이 클래스에 의존한다.
- admin-api `file/` 도메인: Service 클래스 없이 컨트롤러(`FileApiController`)가 `com.tastyhouse.external.file.FileService`를 직접 호출.
- 참고 패턴: `payment`, `verification` 도메인은 core에 `application/port`(및 `application/port/out`) 패키지를 두고 outbound 포트를 명시적으로 분리해둔 선례가 core-module에 이미 있다. `file` 도메인에도 이 패턴을 적용하는 것이 이 프로젝트 관례에 부합한다.

## 작업 지시

### 5-1. 책임 재배치 설계

1. `FileService`의 현재 메서드를 다음 두 그룹으로 분류한다:
   - **순수 저장 어댑터 역할**(S3/Firebase에 바이트를 쓰고 URL을 반환하는 것): `FileStorageStrategy` 구현체가 이미 담당하므로 그대로 유지.
   - **유스케이스 로직**(업로드 전 검증 규칙, 여러 저장소 중 어떤 걸 쓸지 판단, 원격 URL에서 다운로드해 재업로드하는 오케스트레이션 등): core-module `file/application`으로 이동 대상.
2. core-module의 `file` 도메인에 `application/port/out/` 패키지를 신설하고, `FileStoragePort`(가칭) 인터페이스를 정의한다 — `payment`/`verification`의 `application/port` 네이밍 관례를 따른다.
3. `external-api`의 `FileStorageStrategy` 구현체(S3/Firebase)가 이 신규 포트를 구현하도록 리팩터링하거나, 기존 `FileStorageStrategy`를 그대로 두고 core의 신규 application 서비스가 이를 호출하는 구조로 정리한다(둘 중 실제 코드를 보고 더 적은 변경으로 목표를 달성하는 쪽 선택).
4. 원격 이미지 다운로드처럼 순수 HTTP 클라이언트 로직은 유스케이스가 아니라 어댑터 성격이 강하므로, core로 옮기지 않고 external-api 내에 남기되 검증·오케스트레이션 로직과는 분리된 별도 클래스로 추출한다.

### 5-2. 호출부 마이그레이션

1. web-api/admin-api에서 `com.tastyhouse.external.file.FileService`를 직접 import하는 20+9개 지점을 core의 신규 `file/application` 서비스를 호출하도록 변경한다(다른 도메인들이 core의 `application` 서비스를 호출하는 기존 패턴과 동일하게).
2. admin-api `file/` 도메인에 `FileApiController`가 core를 직접 호출하지 않도록 신규 `FileService`(admin-api 계층, core를 감싸는 얇은 Service)를 생성한다 — 다른 admin-api 도메인들과 계층 구조를 맞춘다.

### 5-3. 영향 범위 확인

- web-api 20+ 패키지, admin-api 9개 패키지의 호출부를 전부 grep으로 나열한 뒤 하나씩 마이그레이션한다. 이 작업은 범위가 크므로 도메인별로 나눠 순차 진행해도 된다(예: `order`→`review`→`product`→... 순).

## 완료 기준

- [ ] core-module `file` 도메인에 outbound 포트(`application/port/out`)가 정의됨
- [ ] `external-api`의 file 관련 클래스가 검증/오케스트레이션 로직 없이 순수 저장 어댑터로만 구성됨
- [ ] web-api/admin-api의 모든 호출부가 `com.tastyhouse.external.file.FileService`를 직접 import하지 않고 core의 application 서비스를 통해 접근함
- [ ] admin-api `file/` 도메인에 다른 도메인과 동일한 계층 구조(Controller→Service→core)가 생김
- [ ] 기존 업로드/다운로드 동작이 변경되지 않음(순수 구조 리팩터링)

## 주의사항

- **이 작업은 범위가 크다(30개 가까운 호출부).** 한 번에 다 바꾸지 말고, 먼저 포트/서비스 구조를 완성한 뒤 호출부를 도메인별로 순차 마이그레이션할 것을 권장한다.
- 원격 다운로드 로직(`java.net.http.HttpClient` 사용부)을 core로 옮기면 안 된다 — core-module은 Spring Web/외부 I/O 의존이 없어야 한다는 원칙(core-module AGENTS.md)에 위배된다. 이 로직은 external-api에 남긴다.
- 작업지시서 02(PathBuilder 복원)가 먼저 끝나 있으면 "정식 포트/타입 참조" 패턴에 대한 팀의 최근 감각이 남아 있어 이 작업이 더 매끄러울 수 있으나, 강제 선행조건은 아니다.
- NO_COMMIT_OR_ROLLBACK — 추천 커밋 메시지: `refactor(file): FileService 유스케이스 로직을 core-module로 분리, external-api는 순수 저장 어댑터로 축소`. 호출부 마이그레이션이 여러 커밋으로 나뉜다면 각각 `refactor(file): {도메인} 도메인의 FileService 직접 의존 제거` 형식 권장.

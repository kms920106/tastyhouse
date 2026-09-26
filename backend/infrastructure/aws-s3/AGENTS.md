<!-- Parent: ../../AGENTS.md -->

# infrastructure:aws-s3

AWS S3 파일 저장 어댑터를 소유하는 모듈(`java-library`). 도메인 포트 `FileStoragePort`를 `S3FileStorage`가 직접 구현한다. 파일 저장 벤더의 교체 선택지(firebase ↔ s3) 중 s3 쪽이며, 선택은 스타터 `infrastructure:file-storage`가 한다.

## ⚠️ 어느 앱도 이 모듈을 의존하지 않는다

**현재 파일 저장 벤더는 firebase다.** `infrastructure:file-storage`가 `runtimeOnly project(':infrastructure:firebase')`를 조립하고 `file.provider: firebase`를 소유하므로, 이 모듈은 어떤 앱의 클래스패스에도 없고 `settings.gradle` 포함으로 **컴파일만 검증**된다. jar가 없으니 `AwsS3ModuleAutoConfiguration`도 발화하지 않는다.

`.env`에 `S3_BUCKET_NAME`·`AWS_S3_ACCESS_KEY`·`AWS_S3_SECRET_KEY`가 없다. `application-aws-s3.yml`을 import하는 곳이 없으므로 미해결 플레이스홀더가 부팅을 막지 않는다 — `@ConfigurationProperties` 바인딩은 해당 빈이 등록될 때만 일어난다.

### 한계 — 부패 방지 수단이 컴파일뿐이다

어댑터 테스트가 없다. 지켜지는 범위는 "컴파일이 깨지지 않는다"까지이며 **런타임 동작(SDK 호출·자격증명 로딩·리전)은 검증되지 않는다.** 사용자 결정으로 수용된 한계이며, 전환할 때 아래 절차 뒤에 실제 기동을 직접 확인한다.

## 이 모듈이 따로 있는 이유 (3분할, 2026-09-26)

과거에는 S3·SES·SNS가 벤더 단위 모듈 `infrastructure:aws` 하나에 있었다. 그 구조에 실측으로 확인된 결함이 둘 있었다(분할 중 BOM 전파 결함이 하나 더 드러났다 — 아래 §Dependencies).

1. **전이 의존 누출.** 옛 모듈은 SES·SNS 때문에 `infrastructure:external`(+ webflux)과 `infrastructure:messaging`을 `implementation`으로 가졌다. `implementation`은 `runtimeElements`에 실리므로, 스타터를 aws로 바꾸는 순간 4앱 전부의 `runtimeClasspath`에 external·webflux·messaging이 함께 실렸다. messaging은 클래스패스 존재만으로 발화하고 `JavaMailAdapter`(`matchIfMissing = true`)가 `JavaMailSender`를 요구하는데, admin·ceo·batch는 `spring.mail.host`가 없어 그 빈이 없다 → **파일 저장만 바꿨는데 admin·ceo·batch 기동이 깨지는 구조**였다.
2. **`S3Operations` 미등록.** `spring-cloud-aws-s3` 라이브러리만 선언돼 `spring-cloud-aws-autoconfigure`가 클래스패스에 없었다. `file.provider=s3`로 켜면 `S3FileStorage` 생성자 주입이 실패했다.

S3는 4앱이 스타터를 통해, SES·SNS는 web만 직접 활성화한다 — **활성화 경로와 소비 앱이 다른 채널을 한 모듈에 둔 것이 원인**이라 채널마다 모듈을 나눴다. 이 모듈의 의존은 `domain`과 `spring-cloud-aws-starter-s3`뿐이다.

## S3로 전환하는 절차 (앱 무수정)

1. `infrastructure/file-storage/build.gradle`: `runtimeOnly project(':infrastructure:firebase')` → `runtimeOnly project(':infrastructure:aws-s3')`
2. `infrastructure/file-storage/src/main/resources/application-file-storage.yml`: `spring.config.import`를 `classpath:application-aws-s3.yml`로, `file.provider`를 `s3`로
3. `.env`(운영은 배포 환경변수)에 `S3_BUCKET_NAME`·`AWS_S3_ACCESS_KEY`·`AWS_S3_SECRET_KEY`

4앱은 손대지 않는다. 상세는 `../file-storage/AGENTS.md`.

**모듈 없이 provider만 바꾸면 기동 시 실패한다 — 조용한 오동작은 없다.** 스타터의 gradle 의존을 firebase로 둔 채 `file.provider`만 `s3`로 바꾸면 firebase 구현은 조건으로 빠지고 S3 구현은 클래스패스에 없어, persistence의 `FileUrlResolver`·`FileDomainConfig`가 `FileStoragePort`를 찾지 못한다(2026-09-26 실측: `FileUrlResolver`에서 먼저 실패).

## 무엇을 소유하는가

```
com.tastyhouse.external.aws.s3/
├── AwsS3ModuleAutoConfiguration.java  @AutoConfiguration + @ComponentScan(이 패키지) + @EnableConfigurationProperties(S3FileStorageProperties)
├── S3FileStorage.java                 FileStoragePort 직접 구현 @ConditionalOnProperty(file.provider=s3)
└── S3FileStorageProperties.java       file.aws.s3.* (bucketName · baseUrl)
```

진입 설정은 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`로 자기 등록한다.

## yml — `application-aws-s3.yml`

| 블록 | 소비자 |
|---|---|
| `spring.cloud.aws.region.static` · `spring.cloud.aws.credentials.*` | spring-cloud-aws autoconfigure가 만드는 `S3Client` |
| `file.aws.s3.bucket-name` · `base-url` | `S3FileStorageProperties` |

**리전은 `spring.cloud.aws.region.static` 한 곳에만 둔다.** 과거 `file.aws.s3.region`은 `S3FileStorageProperties`에만 바인딩되고 클라이언트에는 전달되지 않는 죽은 값이라, 3분할 때 yml과 record 컴포넌트에서 함께 지웠다. 자격증명은 SES·SNS와 같은 정적 키 형태이며, IAM 역할 체인을 쓰려면 `credentials` 블록을 지우면 된다(판단은 전환 시점의 몫).

## Dependencies

### Internal
- `domain` (implementation) — `FileStoragePort`, `BusinessException`/`ErrorCode`(`FILE_DELETE_FAILED`)

### External
- `io.awspring.cloud:spring-cloud-aws-starter-s3` — 라이브러리가 아니라 **스타터**여야 한다. autoconfigure가 동반돼야 `S3Operations`·`S3Client` 빈이 생긴다(위 결함 2).
- `implementation platform('io.awspring.cloud:spring-cloud-aws-dependencies:3.1.1')` — spring-cloud-aws를 쓰는 모듈이 이것 하나뿐이라 루트가 아니라 여기 둔다. 확인: `grep -rn 'awspring' --include=build.gradle backend`가 이 모듈만 내놓아야 한다.
- **BOM은 `dependencyManagement { imports { mavenBom … } }`가 아니라 Gradle `platform()`으로 선언한다.** `dependencyManagement` 블록은 선언한 모듈 자신의 해석에만 적용되고 소비 모듈로 전파되지 않아, 버전 없이 선언한 `spring-cloud-aws-starter-s3`가 **스타터를 거쳐 이 모듈을 받은 앱에서 `FAILED`(버전 미해석)** 가 된다(2026-09-26 실측: 스타터를 aws-s3로 임시 교체하자 admin-api `runtimeClasspath`에서 FAILED). 옛 `infrastructure:aws`도 같은 결함을 갖고 있었으나 어느 앱도 의존하지 않아 드러나지 않았다. `platform()`은 소비자에게 제약으로 전파되며 fat jar에는 실리지 않는다.

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **jar 실측으로 미포함을 확인한다**: 4개 앱 fat jar 어디에도 `aws-s3-0.0.1-SNAPSHOT.jar`·`spring-cloud-aws-*`가 있으면 안 된다(`unzip -l {앱}/build/libs/{앱}-0.0.1-SNAPSHOT.jar | grep BOOT-INF/lib/`).
- **전환 리허설 실측(2026-09-26)**: 스타터를 임시로 aws-s3로 바꾸면 admin·ceo `runtimeClasspath`에 `infrastructure:messaging`·`infrastructure:external`·webflux가 0건이고(batch는 crawling 경유로 external·webflux를 원래 갖는다), 더미 자격증명(`S3_BUCKET_NAME`·`AWS_S3_ACCESS_KEY`·`AWS_S3_SECRET_KEY`)으로 admin-api가 `Started AdminApiApplication`까지 기동했다 — firebase가 클래스패스에 없으므로 `FileStoragePort`는 `S3FileStorage`다. 실제 업로드는 확인하지 않았다. ceo-api·batch-module 기동은 리허설하지 않았으므로 실제 전환 때 두 앱도 기동 확인 대상에 넣는다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external.aws.s3` 봉인

**대상**: `backend/infrastructure/aws-s3/src/main/java/com/tastyhouse/external/aws/s3/`

`com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 `@ComponentScan("com.tastyhouse.infrastructure")`가 통째로 스캔해 admin·ceo·batch 부팅이 깨진다. 원래 위치 `external.file.s3`로도 되돌리지 않는다 — 외부 연동 모듈마다 겹치지 않는 하위 패키지를 소유해 split package를 피하는 구조다(`external.aws.ses`는 `aws-ses`, `external.aws.sns`는 `aws-sns` 소유).

### 진입 설정은 자기 하위 패키지만 스캔한다

**대상**: `backend/infrastructure/aws-s3/src/main/java/com/tastyhouse/external/aws/s3/AwsS3ModuleAutoConfiguration.java`

과거 `AwsModuleAutoConfiguration`은 `com.tastyhouse.external.aws` 루트를 통째로 스캔했다. 세 모듈로 나뉜 뒤 루트를 스캔하면 형제 모듈이 같은 클래스패스에 있을 때 그 빈까지 이 설정이 등록하게 되므로, 스캔 범위를 `external.aws.s3`로 넓히지 않는다.

### 위 "S3로 전환하는 절차"는 이 모듈의 유일한 활성화 경로다

**대상**: `backend/infrastructure/aws-s3/src/main/java/com/tastyhouse/external/aws/s3/AwsS3ModuleAutoConfiguration.java`

앱 `build.gradle`에 이 모듈을 직접 추가하지 않는다. 벤더 선택은 스타터가 소유한다(`backend/CLAUDE.md` §벤더 선택은 앱이 아니라 스타터 모듈이 한다).

## 코드 주석에서 이관된 설계 근거

<!-- 분류 B. 모듈 구조와 그 근거 -->

### S3 클라이언트 빈은 직접 만들지 않는다

**대상**: `backend/infrastructure/aws-s3/src/main/java/com/tastyhouse/external/aws/s3/S3FileStorage.java` → 생성자의 `S3Operations` 주입

`spring-cloud-aws-starter-s3`의 autoconfigure가 `S3Operations`·`S3Client`를 등록하므로 이 모듈이 손수 정의하지 않는다. 과거 빈 설정 클래스 `S3FileStorageConfig`가 있었으나 내용이 없어 3분할 때 지웠다. **이 전제는 과거에 성립하지 않았다** — 라이브러리만 선언돼 autoconfigure가 없었기 때문이다(위 결함 2). 스타터를 라이브러리로 되돌리면 같은 결함이 컴파일 에러 없이 재발한다.

### 조건부 전략 배선은 반증 테스트로 확인한다

**대상**: `backend/infrastructure/aws-s3/src/main/java/com/tastyhouse/external/aws/s3/S3FileStorage.java` (`@ConditionalOnProperty(file.provider=s3)`)

기동 성공이 곧 이 전략이 선택됐다는 증거가 아니다 — 조건이 거짓이면 빈이 조용히 빠진 채로도 앱은 뜬다. 전환 후 검증은 틀린 provider 값으로 실패를 확인하는 반증 방향으로 한다(위 "모듈 없이 provider만 바꾸면 기동 시 실패한다"가 그 실패 양식).

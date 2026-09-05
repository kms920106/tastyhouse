<!-- Parent: ../../AGENTS.md -->

# infrastructure:firebase

Firebase Storage 파일 저장 전략을 소유하는 벤더 어댑터 모듈(`java-library`). `infrastructure:external` 7모듈 분리(챕터 01)로 코어에서 떨어져 나왔고, 원래 패키지 `external.file.firebase`에서 **`com.tastyhouse.external.firebase`로 옮겼다** — 코어 `ExternalModuleConfig`가 `com.tastyhouse.external.file`을 스캔해서 그 하위에 두면 동반 스캔되기 때문이다(`../external/AGENTS.md`의 패키지 예외 3건).

## 무엇을 소유하는가

```
com.tastyhouse.external.firebase/
├── FirebaseModuleConfig.java       진입점 — 쓰는 앱이 @Import 한다
├── FirebaseStorageConfig.java      FirebaseApp 빈 (서비스 계정 JSON으로 초기화)
├── FirebaseFileStorage.java        FileStorageStrategy 구현 (업로드·URL·삭제)
└── FirebaseStorageProperties.java  file.firebase.* 프로퍼티
```

## 어느 앱이 의존하는가

**4개 앱 전부**(web-api·admin-api·ceo-api·batch-module)가 `implementation project(':infrastructure:firebase')`를 선언하고 `@Import(FirebaseModuleConfig.class)` 한다. **분리된 6개 벤더·채널 모듈 중 전 앱이 쓰는 유일한 모듈이다** — 파일 업로드는 네 앱 모두 필요하기 때문이며, 나머지는 web(oauth·payment·messaging)이나 batch(crawling) 전용이다.

## 진입 설정과 스캔 범위

`FirebaseModuleConfig`(`@Configuration(proxyBeanMethods = false)`)가 `@ComponentScan("com.tastyhouse.external.firebase")` + `@EnableConfigurationProperties(FirebaseStorageProperties.class)`를 갖는다. `@ConfigurationPropertiesScan`을 쓰지 않고 Properties record를 명시 등록하는 것은 이 저장소의 기존 방침이다.

**`@ConditionalOnProperty(name = "file.provider", havingValue = "firebase")`는 `FirebaseFileStorage`·`FirebaseStorageConfig` 두 구현 클래스에 붙어 있고 진입 설정에는 없다.** 따라서 이 설정을 import 하더라도 `file.provider`가 `firebase`가 아니면 빈이 등록되지 않는다 — 그때는 다른 전략(S3)이 등록돼 있어야 하며, 아무 전략도 없으면 코어의 `FileStoragePortAdapter`가 `FileStorageStrategy`를 찾지 못해 **기동 시** 실패한다.

## yml — `application-firebase.yml` (configtree 시크릿 로딩)

이 모듈의 `src/main/resources/application-firebase.yml`을 **4개 앱 전부**가 `spring.config.import`로 로딩한다.

```yaml
spring:
  config:
    import: optional:configtree:${SECRETS_DIR:/etc/tastyhouse/secrets}/

file:
  firebase:
    service-account-json: ${firebase.service-account:}
    storage-bucket: ${FIREBASE_STORAGE_BUCKET}
    base-url: https://firebasestorage.googleapis.com/v0/b/${FIREBASE_STORAGE_BUCKET}/o
```

**configtree의 취지는 파일 "경로"가 아니라 "내용"을 주입하는 것이다.** `SECRETS_DIR` 디렉터리 트리의 각 파일이 "상대경로 = 프로퍼티 키, 파일 내용 = 값"으로 Environment에 흡수되므로, `{SECRETS_DIR}/firebase/service-account` 파일의 JSON 원문 전체가 `firebase.service-account` 프로퍼티가 된다. 이 방식은 **JVM 작업 디렉터리에 의존하지 않는다** — 과거 `file:` 상대경로를 `ResourceLoader`로 읽던 시절에는 실행 위치(`gradlew -p backend` vs `cd backend`, `java -jar`의 CWD, systemd `WorkingDirectory`)마다 성패가 갈려 실제로 부팅 실패를 냈다. Kubernetes/Docker secret 마운트와 코드가 동일해지는 것도 이 방식의 이점이다.

`optional:` 접두어라 디렉터리가 없어도 부팅은 진행되며, 값 부재는 `FirebaseStorageConfig`가 `SECRETS_DIR` 안내를 담은 `IllegalStateException`으로 명확히 실패시킨다(placeholder 해석 오류 같은 불친절한 실패를 남기지 않는다). 자격증명을 `classpath:`로 동봉하지 않는다 — jar·이미지에 키가 박힌다.

## Dependencies

### Internal
- `infrastructure:external` (implementation) — 구현하는 `FileStorageStrategy` SPI와 `ExternalApiException`/`ExternalApiErrorCode`의 소유 모듈
- `domain-module` (implementation) — 예외 계약(`BusinessException`·`ErrorCodeSpec`)의 뿌리

### External
- `com.google.firebase:firebase-admin:9.10.0` — `FirebaseApp`·`Bucket`. **이 SDK를 클래스패스에 올리는 유일한 모듈이다**(분리 전에는 코어를 의존한 4개 앱이 자동으로 받았고, 지금도 4개 앱이 이 모듈을 의존하므로 결과는 같지만 이유가 명시적이다).

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- **빈 배선**: `FirebaseModuleConfig`를 쓰는 앱이 `@Import` 한다. `implementation` 선언만 하고 `@Import`를 빠뜨리면 컴파일은 통과하고 **기동 시** `FileStorageStrategy` 빈 부재로 실패한다.
- **파일 URL 조립은 이 모듈이 아니라 읽기 경로가 담당한다** — `FileStorageStrategy#getFileUrl`(Firebase 경로 인코딩 + `?alt=media`)을 호출하는 것은 `infrastructure:persistence`의 `FileUrlResolver`다. DB에는 URL이 아니라 경로를 저장하므로, `base-url`이 바뀌어도 저장값은 유효하다.

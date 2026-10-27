<!-- Parent: ../../AGENTS.md -->

# infrastructure:solapi

Solapi SMS 발송 **벤더 모듈**(`java-library`). 포트 `SmsSenderPort`(`web-application`의 `com.tastyhouse.application.phoneverification.port.out` — 과거 domain 소유, 02-vendor-ports로 `application`, 앱 마커 제거로 `web-application`)를 `SolapiSmsClient`가 구현한다. SMS 채널의 기본 벤더이며, 앱이 아니라 채널 모듈 `infrastructure:sms`가 `runtimeOnly`로 조립한다. AWS 대안은 `infrastructure:aws-sns`다.

`infrastructure:messaging` 4분할로 신설됐다. 패키지 루트는 `com.tastyhouse.infrastructure.solapi`이고 `SolapiMessageRequest`·`SolapiMessageResponse`는 `SolapiSmsClient`와 같은 패키지의 package-private record다(아래 봉인·가드 목록). ([결정 0040](../../AGENTS.md#decision-0040))

## 무엇을 소유하는가

```
com.tastyhouse.infrastructure.solapi/
├── SolapiModuleConfig.java  @Configuration(proxyBeanMethods = false) + @EnableConfigurationProperties(SolapiProperties) — 스캔 없음(앱 ModuleScanConfig가 com.tastyhouse.infrastructure를 스캔). imports 제거로 SolapiModuleAutoConfiguration에서 리네임
├── SolapiSmsClient.java                  SmsSenderPort 구현 @ConditionalOnProperty(sms.provider=solapi, matchIfMissing=true)
├── SolapiProperties.java                 sms.solapi.*
├── SolapiMessageRequest.java (package-private, 과거 dto/ 하위)
└── SolapiMessageResponse.java (package-private, 과거 dto/ 하위)
```

`application-solapi.yml`은 `sms.solapi.*`(api-key·api-secret·sender-number·base-url·send-many-path)를 담고, 채널의 `application-sms.yml`이 중첩 import로 로딩한다. `sender-number`는 채널 값 `${sms.sender-number}`를 참조한다.

## 어느 앱이 의존하는가

앱은 직접 의존하지 않는다. web-api → `infrastructure:sms`(runtimeOnly) → 이 모듈(runtimeOnly)로 전이된다. 벤더 전환은 채널 모듈에서 한다(`../sms/AGENTS.md`).

## Dependencies

- `infrastructure:restclient` (implementation) — Boot `RestClient.Builder` customizer. Solapi HTTP 호출은 **동기 `RestClient`**다.
- `web-application` (implementation) — `SmsSenderPort`·`SmsSendResult`·`SmsSendFailure` 포트의 소유 모듈(SMS 발송 포트가 web 전용이라 web 앱 모듈에 있다). `SMS_SEND_FAILED`·`SMS_SEND_NO_RESPONSE`·`SMS_SEND_API_ERROR`는 `WebErrorCode`이고 `SmsVerificationService`가 `SmsSendFailure`를 번역한다. `build.gradle`의 프로젝트 의존은 `:infrastructure:restclient`·`:web-application`이다
- `infrastructure:sms`를 의존하지 않는다(순환 방지)
- 테스트: `SolapiSmsClientTest`(4건) — `MockRestServiceServer.bindTo(RestClient.builder())` 기반

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- `sms.provider` 조건은 구현 클래스에 붙어 있다. 기동 성공이 이 벤더가 선택됐다는 증거가 아니다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.infrastructure.solapi` 봉인

**대상**: `backend/infrastructure/solapi/src/main/java/com/tastyhouse/infrastructure/solapi/`

옛 위치 `external.sms.solapi`로 되돌리지 않는다 — 채널 모듈의 스캔에 동반 스캔된다. 이 모듈의 루트는 `com.tastyhouse.infrastructure.solapi`이고 main 클래스는 전부 그 아래에 있어야 한다 — `backend/infrastructure/solapi/src/test/java/com/tastyhouse/infrastructure/solapi/architecture/VendorLayerRulesTest.java` → `shouldResideInModuleRootPackage`이 강제한다. ([결정 0037](../../AGENTS.md#decision-0037))

### 최상위 클래스에 `public`을 붙이지 않는다 (package-private 적용)

**대상**: `backend/infrastructure/solapi/src/main/java/com/tastyhouse/infrastructure/solapi/` 의 모든 최상위 타입 · 가드 `backend/infrastructure/solapi/src/test/java/com/tastyhouse/infrastructure/solapi/architecture/VendorLayerRulesTest.java` → `topLevelClassesShouldNotBePublic`

이 모듈의 최상위 타입은 전부 package-private이다(허용 목록 없음). 앱이 이 모듈을 타입 이름으로 부르지 않고 `ModuleScanConfig`의 문자열 스캔으로만 조립하며, 소비자는 `application`이 소유한 포트로만 주입받기 때문이다. `public`을 붙이면 다른 모듈이 구현에 직접 결합할 수 있게 되므로 가드가 `build/classes/java/main`의 최상위 클래스를 검사해 빌드를 실패시킨다. 생성자·메서드의 `public`은 유지한다.

외부 API wire DTO(`SolapiMessageRequest`·`SolapiMessageResponse`)도 예외가 아니다. **`{루트}.dto` 하위 패키지로 되돌리지 않는다** — package-private 타입은 같은 패키지에서만 보이므로, 그 DTO를 쓰는 Client와 같은 루트 패키지에 있어야 한다. Jackson은 별도 가시성 설정 없이(기본 `CAN_OVERRIDE_ACCESS_MODIFIERS`) package-private record를 (역)직렬화한다. 근거와 전체 범주는 `backend/CLAUDE.md`의 "접근 제어자 규칙 (내부 구현은 package-private)" 절.

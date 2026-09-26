<!-- Parent: ../../AGENTS.md -->

# infrastructure:solapi

Solapi SMS 발송 **벤더 모듈**(`java-library`). 도메인 포트 `SmsSender`를 `SolapiSmsClient`가 구현한다. SMS 채널의 기본 벤더이며, 앱이 아니라 채널 모듈 `infrastructure:sms`가 `runtimeOnly`로 조립한다. AWS 대안은 `infrastructure:aws-sns`다.

`infrastructure:messaging` 4분할(2026-09-26)로 신설됐다. 패키지는 `external.sms.solapi` → `external.solapi`로 옮겼다 — 채널 모듈의 `@ComponentScan("com.tastyhouse.external.sms")`에 동반 스캔되지 않게 하기 위함이다.

## 무엇을 소유하는가

```
com.tastyhouse.external.solapi/
├── SolapiModuleAutoConfiguration.java    @AutoConfiguration + @ComponentScan(이 패키지) + @EnableConfigurationProperties(SolapiProperties)
├── SolapiSmsClient.java                  SmsSender 구현 @ConditionalOnProperty(sms.provider=solapi, matchIfMissing=true)
├── SolapiProperties.java                 sms.solapi.*
├── request/SolapiMessageRequest.java
└── response/SolapiMessageResponse.java
```

`application-solapi.yml`은 `sms.solapi.*`(api-key·api-secret·sender-number·base-url·send-many-path)를 담고, 채널의 `application-sms.yml`이 중첩 import로 로딩한다. `sender-number`는 채널 값 `${sms.sender-number}`를 참조한다.

## 어느 앱이 의존하는가

앱은 직접 의존하지 않는다. web-api → `infrastructure:sms`(runtimeOnly) → 이 모듈(runtimeOnly)로 전이된다. 벤더 전환은 채널 모듈에서 한다(`../sms/AGENTS.md`).

## Dependencies

- `infrastructure:restclient` (implementation) — Boot `RestClient.Builder` customizer. Solapi HTTP 호출은 **동기 `RestClient`**다.
- `domain` (implementation) — `SmsSender` 포트 + `BusinessException`·`ErrorCode`(`SMS_SEND_FAILED`·`SMS_SEND_NO_RESPONSE`·`SMS_SEND_API_ERROR`)
- `infrastructure:sms`를 의존하지 않는다(순환 방지)
- 테스트: `SolapiSmsClientTest`(4건) — `MockRestServiceServer.bindTo(RestClient.builder())` 기반

## 주의

- **실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- `sms.provider` 조건은 구현 클래스에 붙어 있다. 기동 성공이 이 벤더가 선택됐다는 증거가 아니다.

## 봉인·가드 목록

<!-- 분류 A. 코드 변경을 금지·제약하는 항목. 역참조 앵커 필수 -->

### 자바 패키지 `com.tastyhouse.external.solapi` 봉인

**대상**: `backend/infrastructure/solapi/src/main/java/com/tastyhouse/external/solapi/`

`external.sms.solapi`로 되돌리면 채널 모듈 `infrastructure:sms`의 스캔에 동반 스캔된다. `com.tastyhouse.infrastructure` 아래로 옮기면 `PersistenceModuleAutoConfiguration`의 통째 스캔에 걸려 admin·ceo·batch 부팅이 깨진다.

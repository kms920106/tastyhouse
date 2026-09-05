<!-- Parent: ../../AGENTS.md -->

# infrastructure:file-storage

파일 저장 **기능 한 벌**을 묶어 앱에 노출하는 Spring Boot starter 형태의 조립 모듈(`java-library`). 챕터 03에서 신설됐다. **자바 코드가 없다** — 의존 선언(`build.gradle`)과 설정 진입점(`application-file-storage.yml`) 둘뿐이다.

## 왜 있는가

4개 앱(web·admin·ceo·batch)은 파일 저장을 유스케이스 호출로만 쓴다. 컴파일 시점에 어떤 벤더인지 알지 못하고(`runtimeOnly`), 알 이유도 없다. 그런데 챕터 03 이전에는 벤더 선택이 앱마다 **두 곳**(`build.gradle`의 external·firebase 2줄, `application.yml`의 import 2줄)에 드러나 있어 **같은 결정이 8곳에 복제**돼 있었다. 벤더를 바꾸려면 8곳을 고쳐야 했다.

이 모듈이 코어 SPI(`infrastructure:external`)와 벤더 구현(`infrastructure:firebase`)을 함께 묶어 노출하므로, 앱은 **"파일을 저장한다"** 까지만 알고 **"Firebase로"** 는 모른다. `spring-boot-starter-data-redis`가 Lettuce를 고르는 것과 같은 형태다.

## 구성

```
backend/infrastructure/file-storage/
  build.gradle                                  runtimeOnly external + firebase
  AGENTS.md
  src/main/resources/application-file-storage.yml   file.provider + 벤더 yml 중첩 import
```

`compileJava`는 NO-SOURCE로 넘어가고 jar에는 yml만 실린다(비어 있지 않으므로 리소스 로딩에 문제없음).

**전이 메커니즘**: `runtimeOnly`는 `runtimeElements` 변형에 포함되므로, 이 모듈을 `runtimeOnly`로 의존하는 앱의 `runtimeClasspath`에 external·firebase가 전이로 실린다. compileClasspath에는 셋 다 나타나지 않는다(헥사고날 강제 유지).

**중첩 `spring.config.import`**: `application-file-storage.yml`이 다시 `classpath:application-firebase.yml`을 import 한다. config data 파일 안에서 동작하며, `classpath:` 리소스는 다른 jar(firebase)에 있어도 해석된다(분리 전 허브 `application-external.yml`이 같은 방식으로 6개를 import했던 선례). `application-firebase.yml`의 configtree import(`optional:configtree:${SECRETS_DIR}/`)는 그대로 firebase 모듈이 소유한다.

**순환 없음**: file-storage → external, file-storage → firebase, firebase → external. 이 모듈을 의존하는 것은 앱 4개뿐이다.

## 벤더 전환 절차 (Firebase → S3)

**앱을 건드리지 않는다.** 이 모듈의 두 파일만 바꾼다.

1. `build.gradle`: `runtimeOnly project(':infrastructure:firebase')` → `runtimeOnly project(':infrastructure:aws')`
2. `application-file-storage.yml`: import를 `classpath:application-aws.yml`로, `file.provider: s3`로

메일(SES)·SMS(SNS)는 web 전용 채널이라 이 스타터를 거치지 않는다 — 기존 절차(`../aws/AGENTS.md`)를 그대로 따른다.

## ⚠️ 이 모듈에 코드를 넣지 않는다

**자바 소스를 추가하지 않는다.** 이 모듈의 존재 이유는 "무엇을 조립하는가"를 한 파일에서 읽히게 하는 것이고, 코드가 들어오는 순간 조립 선언과 구현이 섞여 그 가독성이 사라진다. 또한 이 모듈은 auto-configuration을 갖지 않는다(`META-INF/spring/...AutoConfiguration.imports` 없음) — 빈 등록은 조립 대상인 external·firebase의 auto-configuration이 각자 수행한다.

파일 저장 관련 코드가 필요해지면 소속은 둘 중 하나다.

- 벤더 무관 로직·SPI → `infrastructure:external`(코어)
- 특정 벤더 구현 → `infrastructure:firebase` / `infrastructure:aws`

## 주의

- **이 모듈은 실행 단위가 아니다** — `bootJar` 비활성 + plain jar.
- `file.provider` 값은 이 모듈이 소유한다. 챕터 03 이전에는 `infrastructure:external`의 `application-external.yml`이 소유했으나, 그 파일은 이 값만 담고 있어 이동 후 삭제됐다. `FileStorageProperties`(`file.*`) 바인딩 대상은 그대로 `infrastructure:external`에 있다 — 값의 출처만 바뀌었다.
- 기동 성공이 곧 스타터 경유 배선의 증명이다. `FileStoragePortAdapter`가 `FileStorageStrategy`(Firebase 구현) 빈을 주입받으므로, 전이 의존이 끊기면 컨텍스트 로딩이 실패한다.

# ADR-0022: infrastructure:aws를 aws-s3·aws-ses·aws-sns 3모듈로 나눈다

- 상태: 채택
- 결정 커밋: `15e76e263` (2026-09-26)
- 대체한 결정: [ADR-0016](0016-external-vendor-channel-split.md)(일부 — AWS 어댑터를 `infrastructure:aws` 한 벤더 모듈로 묶은 부분)

## 맥락

AWS 어댑터 3종은 채널이 서로 다릅니다.

- S3: 파일 저장
- SES: 메일
- SNS: SMS

[ADR-0016](0016-external-vendor-channel-split.md)의 external 분리 때 이 셋은 **벤더 축**으로 묶여 `infrastructure:aws` 하나가 됐고, 어느 앱도 이 모듈을 의존하지 않았습니다. 그런데 S3는 `file-storage` 스타터를 통해 4앱 전부가, SES·SNS는 web만 활성화합니다. 한 모듈에 묶여 있으면 파일 저장을 S3로 바꾸는 순간 aws 모듈의 전이 의존(코어 `infrastructure:external`·webflux·`messaging`)이 admin·ceo·batch runtimeClasspath에 함께 실립니다. 그러면 `MessagingModuleAutoConfiguration`이 그 앱들에서 발화합니다. 2026-09-26에 이 위험을 실측으로 확인했습니다.

함께 잠재 결함도 하나 있었습니다. `spring-cloud-aws-s3` 라이브러리만 선언돼 autoconfigure가 없었기 때문에, `file.provider=s3`로 바꾸면 `S3Operations` 빈이 등록되지 않아 주입이 실패했습니다.

## 이전 규칙

### backend/CLAUDE.md — 모듈 경계 규칙 (계층 × 앱 2차원 + 기술별 infrastructure)

처음 판단은 "AWS 어댑터를 `aws/` 벤더 패키지로 모으지 않는다"(비채택 대안 3)였다. 제공자를 고르는 축이 벤더가 아니라 채널이고, 자격증명도 채널별로 따로라서 공유할 AWS 설정 코드가 없다는 이유였다. 실제로 당시 `application-aws.yml`은 `file.aws.s3.*`·`mail.aws.ses.*`·`sms.aws.sns.*` 세 채널 블록을 따로 가졌다.

external 분리 때 이 대안을 채택으로 바꿔 AWS 어댑터를 `infrastructure:aws` 하나로 묶었다. 근거는 설정 공유가 아니라 **의존 무게**였다. AWS SDK 3종은 어느 앱도 활성화하지 않는데 4앱 전부의 클래스패스에 실려 있었다. 그래서 벤더로 묶는 목적이 "설정 재사용"에서 "쓰지 않는 SDK를 한 덩어리로 떼어내기"로 바뀌었고, 그 목적에는 벤더 축이 맞았다. 그 결과 `infrastructure:aws`는 어느 앱도 의존하지 않는 모듈이 됐다(`settings.gradle` 포함으로 컴파일만 검증).

전환 절차는 채널별로 달랐다.

- 파일 저장(S3): 앱을 건드리지 않고 `infrastructure/file-storage`의 두 파일만 바꿨다. `build.gradle`의 firebase 한 줄을 aws로 바꾸고, `application-file-storage.yml`의 import를 `classpath:application-aws.yml` + `file.provider: s3`로 바꿨다.
- 메일(SES)·SMS(SNS): web 전용 채널이라 스타터를 거치지 않았다. web `build.gradle`에 aws를 추가하고, web yml에 `application-aws.yml`을 import하고, provider를 바꿨다.

### backend/infrastructure/aws-s3/AGENTS.md — 진입 설정은 아무것도 스캔하지 않는다

과거 `AwsModuleAutoConfiguration`은 `com.tastyhouse.external.aws` 루트를 통째로 스캔했다.

## 결정

어댑터(채널) 단위로 `aws-s3`·`aws-ses`·`aws-sns` 3모듈로 나눕니다. 각 활성화 경로는 자기 의존만 끌고 갑니다.

| 항목 | before | after |
|---|---|---|
| 모듈 | `infrastructure:aws` 1개 | `infrastructure:aws-s3` · `aws-ses` · `aws-sns` 3개 |
| S3 모듈 의존 | 코어 + `messaging`(+webflux) + SDK 전부 | domain + `spring-cloud-aws-starter-s3`만 |
| SES 모듈 의존 | (위와 공유) | domain + 코어 + `messaging`(`MailProperties` 때문) |
| SNS 모듈 의존 | (위와 공유) | domain + 코어(`messaging` 의존 없음) |
| S3 SDK 선언 | `spring-cloud-aws-s3` 라이브러리(autoconfigure 없음) | `spring-cloud-aws-starter-s3`(`S3Operations` 자동 등록) |
| spring-cloud-aws BOM | 루트 `build.gradle`의 `dependencyManagement` 블록 | `infrastructure/aws-s3/build.gradle`에 `platform()`으로 선언 |
| 설정 스캔 범위 | `com.tastyhouse.external.aws` 루트 전체 | 각 모듈 자기 패키지만 |
| S3 전환 시 admin·ceo·batch 클래스패스 | 코어·webflux·messaging 함께 실림 | S3 의존만 실림 |
| 어느 앱이 의존하나 | 없음 | 없음(그대로, 파일 저장은 firebase 유지) |

## 근거

- 채널마다 활성화하는 앱 집합이 다릅니다(S3는 4앱, SES·SNS는 web). 한 모듈에 묶으면 가장 넓은 활성화 경로가 가장 무거운 전이 의존을 끌고 갑니다. 그 결과 쓰지 않는 메시징 auto-configuration이 admin·ceo·batch에서 발화하는 위험이 실측으로 확인됐습니다.
- 벤더로 묶은 근거(쓰지 않는 SDK 떼어내기)는 "어느 앱도 의존하지 않는 상태"에서만 성립했습니다. 한 채널이라도 활성화하면 그 근거가 오히려 불필요한 의존을 끌고 오는 원인이 됩니다.
- 3모듈로 나누면서 스캔 범위도 각자 자기 패키지로 좁혀야 합니다. 루트를 스캔하면 형제 모듈이 같은 클래스패스에 있을 때 그 빈까지 등록하게 됩니다.

## 결과·감수한 손실

- 잠재 결함 2건을 함께 고쳤습니다. ① starter-s3로 바꿔 `S3Operations` 자동 등록이 실제로 일어납니다. ② `dependencyManagement` 블록 방식의 BOM은 소비 앱으로 전파되지 않아 앱에서 버전 미해석(FAILED)이 났으므로 `platform()`으로 바꿨습니다. "라이브러리 BOM은 `platform()`으로 선언한다"는 규칙을 `backend/AGENTS.md`에 추가했습니다.
- 동작 변경으로, S3 전환 시 자격증명은 기본 체인이 아니라 `spring.cloud.aws.credentials` 정적 키를 씁니다.
- 이 분할로 backend 모듈 수가 21개에서 23개가 됐습니다(루트 `CLAUDE.md` "리포지토리 구조"의 모듈 수 연대기).
- 세 모듈 모두 여전히 어느 앱도 의존하지 않으므로, 부패를 막는 수단은 컴파일뿐입니다.
- 같은 날 messaging 4분할로 `MailProperties`가 삭제되고 `SesConfig`가 `mail.sender-address`를 `@Value` 키로 읽게 되며 `aws-ses`의 messaging 의존도 사라졌습니다. 이후 코어 예외 계약 해체로 `aws-ses`·`aws-sns`의 코어 의존도 없어져, 지금 두 모듈의 의존은 SDK와 포트 소유 모듈뿐입니다.

## 영향받은 문서

- `backend/CLAUDE.md` · "모듈 경계 규칙 (계층 × 앱 2차원 + 기술별 infrastructure)" · AWS 어댑터는 채널(어댑터) 단위 3모듈로 나뉘고, 어느 앱도 의존하지 않는다
- `backend/infrastructure/aws-s3/AGENTS.md` · "이 모듈이 따로 있는 이유 (3분할, 2026-09-26)" / "S3로 전환하는 절차 (앱 무수정)" · S3만 4앱 경로이므로 따로 둔다
- `backend/infrastructure/aws-ses/AGENTS.md` · "이 모듈이 따로 있는 이유 (3분할, 2026-09-26)" / "SES로 전환하는 절차 (채널 모듈 2파일)"
- `backend/infrastructure/aws-sns/AGENTS.md` · "이 모듈이 따로 있는 이유 (3분할, 2026-09-26)" / "SNS로 전환하는 절차 (채널 모듈 2파일)"
- `backend/AGENTS.md` · "Key Files" · `build.gradle` 행 — spring-cloud-aws BOM은 `infrastructure/aws-s3/build.gradle`로 이관
- `backend/infrastructure/aws-s3/AGENTS.md` · "Dependencies" > "External" · 라이브러리 BOM은 `dependencyManagement`가 아니라 Gradle `platform()`으로 선언한다
- `CLAUDE.md` · "리포지토리 구조" · 외부 연동 모듈 목록의 `aws-s3`·`aws-ses`·`aws-sns`(어느 앱도 의존하지 않고 컴파일만 검증)

# ADR-0016: infrastructure:external을 벤더·채널별 모듈로 분리한다(기술별 분할 채택)

- 상태: 채택(aws 벤더 묶음은 [ADR-0022](0022-aws-module-three-way-split.md), 파일 저장 코어 SPI는 [ADR-0021](0021-file-storage-spi-removal.md)로 대체)
- 결정 커밋: `109fb06de` (2026-09-05)
- 대체한 결정: —

## 맥락

외부 시스템 연동(OAuth 4종·토스 결제·메일 JavaMail/SES·SMS Solapi/SNS·파일 Firebase/S3·BBQ 크롤링·행정동 경계)과 무거운 벤더 SDK(AWS SES/SNS/S3, Firebase Admin)가 모듈 `infrastructure:external` 하나에 들어 있었고, **4개 앱이 그것을 통째로 받았다.**

- **벤더**: 특정 외부 서비스 구현(예: Solapi, Firebase).
- **채널**: 기능 단위(메일, SMS, 결제 등). 한 채널에 벤더가 여럿일 수 있다.

실사용은 그렇지 않았다(`backend/infrastructure/restclient/AGENTS.md` "분리 배경" 원문).

| 앱 | 실제로 쓰는 어댑터 |
|---|---|
| web-api | OAuth 4종 · Toss · Mail · SMS · File |
| admin-api | File만 |
| ceo-api | File만 |
| batch-module | File(원격 이미지) · BBQ · 행정동 경계 |

admin/ceo/batch가 메일·SMS 어댑터까지 들여와야 했던 직접 원인은 persistence의 `MailDomainConfig`·`SmsDomainConfig`가 `MailSenderPort`/`SmsSenderPort` 빈을 무조건 요구한 것이었다.

## 이전 규칙

### `backend/CLAUDE.md` — "external을 infrastructure 아래로 들인 이유"의 비채택 대안 (`c52280a87`, 2026-09-05 08:35)

- **비채택 대안 (1) 기술별 추가 분할**: "3~7파일짜리 모듈 6~7개와 `build.gradle` 보일러플레이트 복제, 공통 자산(`WebClientConfig`·`ExternalApiException`) 분리 비용이 이득을 넘는다."
- **비채택 대안 (3) AWS 어댑터를 `aws/` 벤더 패키지로 모으기**: "제공자 선택 축이 벤더가 아니라 채널이고 자격증명도 채널별로 따로여서 공유할 AWS 설정 코드가 없다."
- 이 대안들은 같은 날 앞선 챕터 01에서 `external-api` 모듈을 `infrastructure:external`로 편입할 때 기록됐다. `-api` 접미어는 인바운드 어댑터를 뜻하는데 `external-api`는 아웃바운드 클라이언트 모음이라 이름이 역할을 거꾸로 가리켰고, 편입 후 driven 어댑터 3형제 `persistence`·`redis`·`external`이 `infrastructure` 아래 나란히 놓였다. 이때 외부 연동 전체는 여전히 `external` 한 모듈이었다.

### `backend/infrastructure/restclient/AGENTS.md` — Dependencies / Internal

분리 전 코어(`infrastructure:external`)는 소셜 로그인 SPI·BBQ·행정동 경계 포트를 구현하느라 `application`을 `implementation`으로 의존했다. 분리로 그 어댑터들이 oauth·crawling 모듈로 떠나 코어에는 아웃바운드 계약 소비자가 남지 않았고, `application` 의존도 없어졌다.

### `backend/infrastructure/restclient/src/main/java/com/tastyhouse/infrastructure/restclient/AGENTS.md` — 다른 모듈로 이동한 패키지

분리 직후 코어 디렉터리의 패키지는 다음처럼 옮겨졌다(이름은 그 시점 기준이며, 이후 infrastructure 패키지 루트 통일로 전부 `com.tastyhouse.infrastructure.*`가 됐다).

| 과거 패키지 | 분리 직후 |
|---|---|
| `oauth/{kakao,naver,apple,facebook}` | `infrastructure:oauth`(패키지 불변) |
| `payment/toss` | `infrastructure:payment`(패키지 불변) |
| `mail/`, `mail/javamail`, `sms/`, `sms/solapi` | `infrastructure:messaging`(패키지 불변) |
| `mail/ses` · `sms/sns` · `file/s3` | `infrastructure:aws`(`external.aws.ses`·`external.aws.sns`·`external.aws.s3`로 변경) |
| `file/firebase` | `infrastructure:firebase`(`external.firebase`로 변경) |
| `crawling/bbq`, `region/` | `infrastructure:crawling`(패키지 불변) |
| `file/RemoteImageDownloader` | `infrastructure:crawling`(`external.crawling`으로 변경) |
| `file/ByteArrayMultipartFile` | 삭제(전략 인터페이스가 `byte[]`를 받게 되어 래퍼가 불필요) |

벤더 패키지를 `external.file` 아래에 두지 않은 이유는 코어 `external.file` 스캔이 하위 패키지를 동반 스캔하기 때문이다(아래 "결과·감수한 손실"의 형제 패키지 이동).

## 결정

대안 (1)·(3)을 채택으로 뒤집는다. `infrastructure:external`을 7모듈로 나누고, 앱은 실사용 어댑터만 의존한다.

| 항목 | before | after |
|---|---|---|
| 모듈 | `infrastructure:external` 1개 | `external`(코어: WebClient·예외·파일 전략 SPI) / `firebase` / `aws` / `oauth` / `payment` / `messaging` / `crawling` |
| admin·ceo 의존 | external 전체 | external + firebase |
| batch 의존 | external 전체 | + crawling |
| web 의존 | external 전체 | + oauth + payment + messaging |
| aws | 4앱 클래스패스 | 어느 앱도 의존하지 않고 컴파일만 검증 |
| `MailDomainConfig`·`SmsDomainConfig` | persistence | messaging으로 이관 |
| 모듈 수 | 14 | 20 |

## 근거

- **대안 (1) 번복 근거는 앱별 실사용 실측이다** — admin-api·ceo-api가 실제로 쓰는 어댑터는 파일 저장 하나뿐인데 OAuth·Toss·메일·SMS·크롤링과 Firebase Admin·AWS SDK 3종을 통째로 받고 있었다. 공통 자산은 코어에 남기고 6모듈이 그것을 의존하므로 "분리 비용"으로 본 것은 실재하지 않았다. `build.gradle` 보일러플레이트 복제(7벌)는 그대로지만 앱이 안 쓰는 SDK를 배포 산출물에서 빼는 이득이 더 크다.
- **대안 (3) 번복 근거는 설정 공유가 아니라 의존 무게였다** — "공유할 AWS 설정 코드가 없다"는 관찰 자체는 맞다. 다만 AWS SDK 3종은 어느 앱도 활성화하지 않는데 4앱 전부에 실려 있었다. 벤더로 묶는 목적이 "설정 재사용"에서 "쓰지 않는 SDK를 한 덩어리로 떼어내기"로 바뀌었다.
- 기준은 "앱별 실사용 차이"다 — 이 기준은 지금도 외부 연동 모듈을 쪼개는 기준이다.

## 결과·감수한 손실

- 동작 변경 없음(빌드·4앱 기동 검증). `FileStorageStrategy`를 `byte[]` 기반으로 단순화하고 `ByteArrayMultipartFile`을 삭제했다. 코어 `external.file` 스캔이 하위 패키지를 동반 스캔하므로 firebase·s3·ses·sns·`RemoteImageDownloader` 5건은 형제 패키지로 옮겼다(커밋 메시지).
- 이후 같은 기준의 세분화가 이어졌다 — 파일 저장 스타터([ADR-0018](0018-channel-starter-vendor-modules.md)), crawling·messaging·payment·oauth의 채널·벤더 분할, 코어의 `infrastructure:http-client` → `infrastructure:restclient` 리네임과 WebClient 제거·예외 계약 해체.
- **부분 대체**: aws 단일 모듈은 `aws-s3`·`aws-ses`·`aws-sns` 3모듈로 다시 나뉘었고([ADR-0022](0022-aws-module-three-way-split.md)), 코어의 파일 저장 SPI(`FileStorageStrategy`·`FileStoragePortAdapter`)는 삭제돼 벤더가 `FileStoragePort`를 직접 구현한다([ADR-0021](0021-file-storage-spi-removal.md)).

## 영향받은 문서

- `backend/CLAUDE.md` · "모듈 경계 규칙 (계층 × 앱 2차원 + 기술별 infrastructure)" · infrastructure는 기술별로, 외부 연동은 앱별 실사용 차이로 나눈다
- `backend/CLAUDE.md` · "모듈 지도 (모듈 재편 완료 + application 모듈 통합 + external 분리 + 앱 모듈 재분리)" · 현재 모듈 목록
- `backend/infrastructure/restclient/AGENTS.md` · "외부 연동 모듈 지도" · 분리 후 모듈 배치
- `backend/AGENTS.md` · "Module Dependency Graph" · admin·ceo는 `file-storage` 하나만 의존
- `CLAUDE.md` · "리포지토리 구조" · 외부 연동 19모듈의 앱별 실사용 차이

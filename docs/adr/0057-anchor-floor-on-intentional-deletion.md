# ADR-0057: 개수 anchor 하한은 유스케이스를 의도적으로 삭제할 때만 실측값으로 내린다

- 상태: 채택
- 결정 커밋: `0068b7eae` (2026-10-16)
- 대체한 결정: —

## 맥락

- **개수 anchor**: `backend/application-core/src/test/java/com/tastyhouse/application/architecture/RuleAnchorTest.java`와 같은 디렉터리의 `LayerRulesTest.java`는 규칙이 검사하는 대상 클래스의 수가 하한 이상인지 단정합니다. 대상이 0건이 되어 규칙이 아무것도 검사하지 않고 통과하는 상태(공허 통과)를 잡기 위한 장치입니다.
- 유스케이스 분리([ADR-0043](0043-usecase-per-service.md)) 직후 실측값을 하한으로 고정했고, 문서에는 "하한을 낮추지 않는다"고만 적혀 있었습니다.

웹 결제 API `POST /api/payments/v1/confirm`은 소유권·금액·PG 조회 없이 PENDING 결제를 완료 처리할 수 있는 보안 결함이 있었습니다. 프론트가 호출하지 않는 경로라 엔드포인트와 그 전용 유스케이스 2개(`PaymentConfirmUseCase`, `PaymentDetailByIdQueryUseCase`)를 삭제했습니다. 그런데 실측값이 하한과 정확히 같아서(web UseCase 187, 명령 서비스 314, 조회 서비스 268, 유스케이스 서비스 582), 삭제만으로 가드 테스트가 실패했습니다. "낮추지 않는다"를 문자 그대로 따르면 쓰지 않는 결함 코드를 지울 수 없습니다.

## 이전 규칙

`backend/application-core/AGENTS.md` · "`moduleBeanCounts`·`moduleUseCaseCounts` 하한을 낮추지 않는다" 항목과 "하한 582를 낮추지 않는다" 항목: 예외 없이 하한을 낮추지 않는다.

## 결정

| 상황 | before | after |
|---|---|---|
| 클래스 이동·출처 판정 오류로 개수가 줄어듦 | 낮추지 않음 | 낮추지 않음(변경 없음) |
| 유스케이스를 의도적으로 삭제함 | 규칙 없음(삭제가 막힘) | 삭제 후 실측값으로 하한을 맞춘다 |

이번 변경에서 맞춘 값: `moduleUseCaseCounts` web 187 → 185, `commandServicesExist` 314 → 313, `queryServicesExist` 268 → 267, `LayerRulesTest`의 유스케이스 하한 2곳 582 → 580. `inboundPortsExist`는 ArchUnit이 중첩 타입까지 세어 실측이 894였고 삭제 후에도 891 = 하한이라 바꾸지 않았습니다.

## 근거

- anchor가 막으려는 것은 "판정이 깨져 대상이 조용히 사라지는 것"이지, 의도한 삭제가 아닙니다.
- 실측보다 낮게 내리면 가드가 느슨해지므로 "삭제 후 실측값"으로만 맞춥니다. 실측은 파일 수가 아니라 규칙이 세는 방식(중첩 타입 포함 등)으로 잽니다.

## 결과·감수한 손실

- 의도한 삭제인지는 리뷰가 판단합니다. 하한을 내리는 diff에는 삭제한 유스케이스 목록이 함께 있어야 합니다.
- 하한 수치가 `backend/CLAUDE.md`와 `backend/application-core/AGENTS.md`에 여러 번 적혀 있어 함께 고쳐야 합니다.

## 영향받은 문서

- `backend/application-core/AGENTS.md` · "`moduleBeanCounts`·`moduleUseCaseCounts` 하한을 낮추지 않는다" 항목 · 의도적 삭제 시에만 실측으로 맞춘다는 문장 추가
- `backend/application-core/AGENTS.md` · "하한 580을 낮추지 않는다" 항목 · 같은 취지 추가와 수치 갱신
- `backend/CLAUDE.md` · "application 서비스 CQRS 분리 규칙" 절의 가드 표, "인바운드 포트 ArchUnit 규칙 6종" 절의 `RuleAnchorTest` 행 · 하한 수치 갱신

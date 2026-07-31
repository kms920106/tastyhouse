# P7. shop 컨텍스트 로직 이탈 복구 — Calculator 로직을 자식 애그리거트로

## 배경

shop은 model 36 + repository 14 + service 8의 최대 컨텍스트인데, 자식 애그리거트(`ShopBusinessHour`/`ShopBreakTime`/`ShopClosedDay`)가 스스로 답해야 할 질문을 297줄짜리 도메인 서비스 `ShopOperatingStatusCalculator`가 전부 getter로 캐묻고 있다. 특히 enum 상수명 문자열 파싱은 리네이밍 시 런타임에 조용히 깨지는 구조다.

## 문제 상세

`domain-module/src/main/java/com/tastyhouse/domain/shop/domain/service/ShopOperatingStatusCalculator.java` (297줄):

1. `:127-138 isWithinBreakTime` — `breakTime.getStartTime()`/`getEndTime()`/`getDayType()`을 꺼내 판정. `ShopBreakTime.covers(LocalTime, DayOfWeek, boolean)`이 있어야 할 로직.
2. `:100-125 isWithinBusinessHours` — `todayHour.getIs24Hours()`/`getIsClosed()`/`getOpenTime()`/`getCloseTime()`을 `Boolean.TRUE.equals(...)`로 4중 방어. `ShopBusinessHour.isOpenAt(LocalTime)` 부재 + `Boolean` 래퍼 3-상태를 모델이 `boolean`으로 정규화하지 못한 신호.
3. `:217-292 matchesClosedDay` — **`ClosedDayType` enum name 문자열 파싱**: `name.endsWith("MONDAY")`, `name.startsWith("EVERY_WEEK_")`. enum이 `dayOfWeek()`/`weekOrdinal()` 속성을 가져야 할 로직이 문자열 조작으로 구현됨.
4. 반례(잘 된 것): `ShopSuspension.isActive(now)`만 모델에 있음(`:82`).
5. 부가: `Shop` 모델 자체도 setter 10개 수준 — `close()`(폐업) 후 `show()` 노출 복구·`update()` 정보 수정이 가능(`Shop.java:159-230`). 폐업 상태 가드 부재.

## 작업 지시

1. `ClosedDayType` enum에 속성 기반 판정을 추가한다: 각 상수가 `DayOfWeek dayOfWeek`(해당 시), `Integer weekOrdinal`(EVERY_WEEK_N류) 필드를 생성자로 갖고, `boolean matches(LocalDate date, boolean isHoliday)`(시그니처는 현행 판정 입력에 맞춰 조정)를 제공한다. Calculator의 `:217-292` 문자열 파싱을 이 메서드 호출로 교체.
2. `ShopBusinessHour`에 `boolean isOpenAt(LocalTime time)`을 추가한다 — 24시간·휴무·개점/폐점 시각(자정 넘김 케이스 포함, 현행 Calculator 로직 그대로 이식)을 내부에서 판정. `Boolean` 래퍼 3-상태는 모델 내부에서 정규화(`Boolean.TRUE.equals` 방어를 모델 안 한 곳으로).
3. `ShopBreakTime`에 `boolean covers(LocalTime time, DayOfWeek dayOfWeek, boolean isHoliday)`(현행 판정 입력에 맞춰 조정)를 추가하고 Calculator `:127-138`을 교체.
4. Calculator는 "여러 자식 애그리거트를 조합해 최종 영업 상태를 판정"하는 오케스트레이션만 남긴다(파일 자체는 유지 — 크로스 애그리거트 판정은 도메인 서비스 잔류가 정당).
5. `Shop`의 폐업 가드: `close()` 이후 `show()`/`update()` 허용 여부를 확인(admin 화면에 "폐업 취소" 기능이 있는지 호출부 조사). 업무상 금지가 맞으면 상태 가드 추가(`BusinessException`), 허용이 맞으면 그 근거를 Javadoc에 명시. **판단이 안 서면 선택지로 사용자에게 질문.**
6. 신설 메서드에 대한 domain-module 단위 테스트 추가: `ClosedDayTypeTest`(모든 상수 × 대표 날짜), `ShopBusinessHourTest.isOpenAt`(24시간/휴무/자정 넘김 경계), `ShopBreakTimeTest.covers`. **현재 `ShopOperatingStatusCalculator`는 테스트가 0이다** — 이식 전 현행 동작을 고정하는 테스트를 먼저 작성(golden test)한 뒤 리팩터링할 것.

## 수용 기준

- [ ] Calculator에 enum `name()` 문자열 파싱 0건
- [ ] `getIs24Hours()`/`getIsClosed()`류 getter 캐묻기가 Calculator에서 제거되고 모델 메서드 호출로 대체
- [ ] 이식 전후 동작 동일 — golden test로 증명 (영업중/브레이크/휴무/자정넘김/공휴일 케이스)
- [ ] domain-module 테스트 전량 통과 (verify-without-gradle)

## 주의사항

- **P5(write 포트 유출)와 같은 영역(shop)** — P5 먼저 수행 권장. **P4와 `ShopBusinessHour.java` 파일 충돌**(P4가 `of()` 검증 추가) — 담당 조율.
- shop BC 분할(`Station`/`Tag`/`ProhibitedWord` 별도 컨텍스트화)은 이번 범위가 아니다 — 대규모 이동이라 별도 설계 결정 필요. 이 태스크는 로직 배치만 고친다.
- enum에 필드를 추가해도 `EnumType.STRING` 저장이므로 DB 영향 없음(상수 이름 자체는 바꾸지 말 것 — 저장값이다).

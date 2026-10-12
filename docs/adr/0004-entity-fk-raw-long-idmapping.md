# ADR-0004: 엔티티 FK는 raw Long으로 두고 매퍼에서 IdMapping으로 승격·언패킹한다(정책 B)

- 상태: 대체됨(→ [ADR-0028](0028-persistence-state-layer.md))
- 결정 커밋: `f37028ab4` (2026-08-06)
- 대체한 결정: —

## 맥락

도메인 모델은 식별자를 강타입 VO로 다룬다(`record MemberId(Long value)` 같은 **ID VO**). JPA 엔티티의 **크로스 애그리거트 FK**(다른 애그리거트를 가리키는 외래키 컬럼)도 같은 VO로 매핑하고 있었다(정책 A).

- **애그리거트**: 함께 저장·변경되는 도메인 객체 묶음. 다른 애그리거트는 객체가 아니라 식별자로만 참조한다.

정책 A는 QueryDSL과 반복적으로 충돌했다(커밋 메시지) — 파라미터 타입 불일치, 투영 시 VO 반환, 문자열 필드명 우회(`ConvertedIdPaths`·`Expressions.numberPath`)가 다수 필요했다. VO 매핑을 하면 QueryDSL이 `NumberPath<Long>` 대신 VO path를 생성해 조회 DAO의 조인·투영이 깨진다.

## 이전 규칙

### `backend/CLAUDE.md` — ID VO 경계 규칙 (정책 A)

엔티티 FK도 VO로 둔다. FK마다 `AttributeConverter<XxxId, Long>`(`*IdConverter`, 27개)를 만들고 `@Convert`로 매핑한다.

### `backend/AGENTS.md` — Common Patterns

식별자 강타입화: `record MemberId(Long value)`(domain) + `AttributeConverter`(`infrastructure:persistence`의 `<ctx>/persistence/XxxIdConverter`)로 JPA 매핑.

### `backend/CLAUDE.md` — ID VO 경계 규칙 > 엔티티 FK는 raw `Long`, 매퍼가 삼항 null 가드로 승격/언패킹 (정책 B)

이 ADR이 세운 정책 B의 원래 문구다. 이후 [ADR-0028](0028-persistence-state-layer.md)로 후반부(`IdMapping`)가 폐기됐다.

- 매퍼(`<ctx>/persistence/XxxMapper.java`)의 VO↔raw 변환은 `infrastructure/shared/persistence/IdMapping`으로 통일한다. `toDomain`에서는 `IdMapping.vo(entity.getXxxId(), XxxId::of)`, `toEntity`/`applyChanges`에서는 `IdMapping.raw(domain.getXxxId(), XxxId::value)`를 쓰고, `CeoId.of(entity.getCeoId())`처럼 직접 호출하지 않는다.
- NOT NULL 컬럼에서도 예외 없이 통일한다. `raw(...)`는 NOT NULL 컬럼에서도 죽은 코드가 아니다.
- `Xxx.of(...)` 위임 규칙(DTO 조립 규칙)의 예외가 아니다 — `IdMapping.vo(raw, CeoId::of)`의 메서드 레퍼런스가 `of()` 팩토리를 경유하므로 `new`는 여전히 팩토리 내부에만 남는다.

### `backend/infrastructure/jpa/AGENTS.md` — 크로스 애그리거트 FK는 raw `Long`이고, VO 변환은 `IdMapping`이 전담한다

- 절 제목이 "VO 변환은 `IdMapping`이 전담한다"였고, 대상 파일은 `backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/shared/persistence/IdMapping.java`(`IdMapping#vo`·`IdMapping#raw`·각 `*Mapper`)였다.
- raw `Long` 예시로 `CeoReplyPhraseJpaEntity.ceo_id`·`MenuReviewJpaEntity`의 FK 전부를 들었다.
- 덩어리 03b 직후에는 삼항 null 가드의 참고 구현이 `backend/application-core/src/main/java/com/tastyhouse/application/shop/store/ShopStateMapper.java`였고, persistence domain 재허용 이후 `ShopMapper`(`backend/infrastructure/persistence/src/main/java/com/tastyhouse/infrastructure/persistence/shop/persistence/ShopMapper.java`)로 돌아왔다.

## 결정

| 항목 | before (정책 A) | after (정책 B) |
|---|---|---|
| 엔티티 FK 컬럼 타입 | `XxxId` VO + `@Convert` | raw `Long` |
| `*IdConverter` | 27개 | 삭제(`AmountConverter` 등 비-ID VO 컨버터는 유지) |
| VO ↔ raw 변환 | 컨버터가 자동 | 매퍼가 `IdMapping.vo(raw, CeoId::of)` / `IdMapping.raw(vo, CeoId::value)`로 전담 |
| 도메인 모델·domain repository·도메인 서비스 | `XxxId` VO | 불변 — `XxxId` VO 유지 |
| QueryDSL 우회 | `ConvertedIdPaths`·`Expressions.numberPath` | 전량 제거 |

61개 엔티티의 크로스 애그리거트 FK를 raw `Long`으로 바꿨다. **승격·언패킹은 예외 없이 `IdMapping`을 거친다** — nullable 여부와 무관하게 모든 매퍼가 이 헬퍼를 쓴다.

## 근거

- 정책 A에서 QueryDSL 우회(`ConvertedIdPaths`)가 낳은 결함은 세 유형이었다 — (1) 파라미터 바인딩 시 VO/Long 타입 불일치로 `QueryArgumentException`, (2) 투영 시 컨버터가 VO 인스턴스를 돌려줘 `Long`-typed Result 생성자와 불일치(해당 FK 행이 실제로 존재할 때만 터져 빈 테이블 테스트로는 잡히지 않고 오래 숨어 있었다), (3) 한 도메인의 FK를 VO로 전환하면 그 컬럼을 조인하던 다른 도메인의 query DAO가 함께 깨지는 크로스 도메인 회귀. 엔티티 FK를 raw `Long`으로 두면 QueryDSL이 VO path를 만들 일이 없어 세 유형이 구조적으로 사라진다.
- 모든 `XxxId`는 compact constructor에서 null을 거부하는데 일부 FK는 nullable이다(`Shop.ceo_id` 점주 미배정, `BugReport.assignee_admin_id`, `MemberDeliveryAddress.admin_dong_id` 행정동 매칭 실패, `ShopRequestIndex.attachment_file_id`). 매퍼에서 `CeoId.of(entity.getCeoId())`를 직접 호출하면 컴파일은 통과하고 그 FK가 실제로 null인 행을 읽을 때만 `IllegalArgumentException`이 난다 — 빈 테이블이나 FK가 항상 채워진 샘플 데이터로는 잡히지 않는다.
- 쓰기 방향 `raw(...)`도 대칭으로 필요하다 — 도메인 모델이 미배정 상태를 null VO로 들고 있으면 `getCeoId().value()`가 NPE다.
- "NOT NULL이면 직접 호출, nullable이면 헬퍼"처럼 나누면 매번 nullable 여부를 확인해야 하고 위험한 직접 호출이 흔해 보여 눈에 띄지 않는다.

## 결과·감수한 손실

- 동작 변경 없음(커밋 메시지). `IdMapping`/`ShopMapper` nullable 왕복 테스트가 infrastructure 모듈의 첫 테스트 소스셋으로 생겼다.
- **대체**: 덩어리 03b([ADR-0028](0028-persistence-state-layer.md))로 persistence가 `XxxId`를 아예 모르게 되면서 `IdMapping`·`IdMappingTest`·`AmountConverter`가 삭제됐고, 변환은 application `XxxStateMapper`의 삼항 null 가드로 옮겨졌다. 이후 [ADR-0031](0031-persistence-domain-reallowed.md)이 변환 위치를 persistence 매퍼로 되돌렸지만 형태는 삼항 null 가드 그대로이고 `IdMapping`은 되살리지 않았다.
- "엔티티 FK는 raw `Long`"이라는 정책 B의 전반부는 지금도 유효하다.

## 영향받은 문서

- `backend/CLAUDE.md` · "ID VO(식별자 값 객체) 경계 규칙" > "엔티티 FK는 raw `Long`, 매퍼가 삼항 null 가드로 승격/언패킹 (정책 B)" · FK는 raw `Long`, 변환은 persistence 매퍼의 삼항 null 가드
- `backend/infrastructure/jpa/AGENTS.md` · "크로스 애그리거트 FK는 raw `Long`이고, VO 변환은 매퍼의 삼항 null 가드가 한다" · 현재 변환 형태와 nullable FK 목록
- `backend/infrastructure/jpa/AGENTS.md` · "FK 컬럼에 `@Convert`로 VO를 매핑하지 않는다" · QueryDSL path 충돌 근거
- `backend/AGENTS.md` · "Common Patterns" · 식별자 강타입화와 매퍼 변환

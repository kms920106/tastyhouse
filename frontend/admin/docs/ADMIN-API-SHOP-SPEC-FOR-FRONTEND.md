# [Admin API] 가게 관리 변경 스펙 — 프론트엔드 AI 전달용

관리자(admin) 웹의 프론트엔드를 담당하는 AI에게 전달하는 문서입니다. 점주(ceo-api) 가게 설정 기능이 추가되면서 **admin API에 변경/신규된 엔드포인트**만 정리했습니다. 아래 스펙대로 화면·연동을 구현하면 됩니다.

- **Base URL**: 기존 admin API와 동일 (예: `https://{admin-host}`)
- **인증**: 기존과 동일 (관리자 JWT, `Authorization: Bearer {accessToken}`)
- **공통 응답 래퍼**: 기존과 동일 `ApiResponse<T>` (`{ "data": ..., "success": true, ... }`). 목록 페이징은 기존과 동일하게 `content/page/size/totalElements`.
- **날짜/시간 포맷**: `LocalDate`=`YYYY-MM-DD`, `LocalTime`=`HH:mm:ss`, `LocalDateTime`=`YYYY-MM-DDTHH:mm:ss`

---

## 1. [변경] 가게 등록 — 점주 배정 필드 추가

`POST /api/shops/v1` 요청 바디에 **`ceoId`(선택)** 가 추가되었습니다. 가게를 특정 점주(ceo)에게 소유시킬 때 사용합니다. 미지정 시 점주 미배정 가게로 등록됩니다.

```jsonc
// POST /api/shops/v1  (ShopCreateRequest)
{
  "ceoId": 1,            // [신규] 선택. 소유 점주 ID. 없으면 미배정
  "stationId": 1,        // 필수
  "name": "맛있는 분식",  // 필수
  "latitude": 37.497942, // 필수
  "longitude": 127.027621,// 필수
  "roadAddress": "서울시 강남구 테헤란로 1",
  "lotAddress": "서울시 강남구 역삼동 1-1",
  "phoneNumber": "02-1234-5678",
  "thumbnailImageFileId": 10
}
```
> **프론트 반영**: 가게 등록 화면에 "소유 점주 선택"(점주 목록에서 ceoId 선택) UI 추가. 선택 안 하면 null 전송.

---

## 2. [변경] 가게 운영시간 — 24시간 영업 필드 추가

영업시간 등록/수정/조회에 **`is24Hours`(24시간 영업 여부)** 가 추가되었습니다.

```jsonc
// POST /api/shops/v1/{id}/business-hours , PUT /api/shops/v1/business-hours/{businessHourId}
// (ShopBusinessHourSaveRequest)
{
  "dayType": "WEEKDAY",   // 필수. DAILY/WEEKDAY/WEEKEND/HOLIDAY/MONDAY~SUNDAY
  "openTime": "09:00:00",
  "closeTime": "22:00:00",
  "isClosed": false,
  "is24Hours": false      // [신규] true면 openTime/closeTime 무시
}
```
```jsonc
// 응답 (ShopBusinessHourResponse) — 조회/등록 결과
{
  "id": 1,
  "dayType": "WEEKDAY",
  "description": "평일",
  "openTime": "09:00:00",
  "closeTime": "22:00:00",
  "isClosed": false,
  "is24Hours": false      // [신규]
}
```
> **서버 검증(참고)**: 영업시간은 5분 단위, 최소 1시간~최대 23시간 55분, 자정 넘김(종료<시작) 허용. `isClosed=true` 또는 `is24Hours=true`면 시간 검증 생략. 위반 시 400 에러(`SHOP_BUSINESS_HOUR_INVALID_UNIT`/`_INVALID_RANGE`). 프론트에서도 5분 단위 입력 UI 권장.
> **프론트 반영**: 영업시간 편집 화면에 "24시간 영업" 토글 추가. 켜지면 시간 입력 비활성화.

---

## 3. [신규] 이미지 변경요청 검수 (상표/대표이미지 승인·반려)

점주가 상표(TRADEMARK)·대표이미지(THUMBNAIL) 변경을 요청하면 관리자가 검수합니다.

### 3-1. 검수 대기/이력 목록 조회
```
GET /api/shops/v1/image-change-requests?status={status}&imageType={imageType}&page=0&size=10
```
- 쿼리 파라미터(모두 선택): `status`(`PENDING`/`APPROVED`/`REJECTED`), `imageType`(`TRADEMARK`/`THUMBNAIL`), `page`, `size`
- 응답: 페이징 목록, 각 항목 `ShopImageChangeRequestItemResponse`:
```jsonc
{
  "id": 100,
  "shopId": 5,
  "imageType": "TRADEMARK",   // TRADEMARK(상표) / THUMBNAIL(대표이미지)
  "imageFileId": 320,          // 요청된 신규 이미지 파일 ID
  "status": "PENDING",         // PENDING/APPROVED/REJECTED
  "rejectReason": null         // 반려 시 사유
}
```

### 3-2. 승인
```
PATCH /api/shops/v1/image-change-requests/{requestId}/approve
```
- 바디 없음. 승인 시 해당 이미지가 가게에 즉시 반영됩니다(상표→가게 상표, 대표→썸네일).
- 이미 PENDING이 아니면 409(`SHOP_IMAGE_CHANGE_REQUEST_NOT_PENDING`).

### 3-3. 반려
```
PATCH /api/shops/v1/image-change-requests/{requestId}/reject
```
```jsonc
// ShopImageChangeRejectRequest
{ "reason": "저작권 확인이 불가한 이미지입니다." }
```
> **프론트 반영**: "이미지 검수" 화면 신설 — 상태별 필터 목록 + 이미지 미리보기(imageFileId로 파일 조회) + 승인/반려(사유 입력) 버튼.

---

## 4. [신규] 콘텐츠보드 검수 (숨김/삭제)

점주가 등록한 콘텐츠보드는 즉시 노출되며, 관리자가 사후에 규정 위반 시 숨김/삭제합니다.

### 4-1. 숨김/노출 토글
```
PATCH /api/shops/v1/content-boards/{contentBoardId}/hide
```
```jsonc
// ShopContentBoardHideRequest
{ "hidden": true }   // true=숨김, false=노출복원
```

### 4-2. 삭제
```
DELETE /api/shops/v1/content-boards/{contentBoardId}
```
> **참고**: 콘텐츠보드 목록 자체 조회는 점주(ceo) API에 있으며, 관리자용 검수 목록 조회 API는 이번 범위에 별도로 없습니다. 필요 시 요청 바랍니다(현재는 숨김/삭제 조치만 제공).
> **프론트 반영**: 콘텐츠보드 신고/검수 화면에서 개별 콘텐츠 숨김 토글·삭제 버튼.

---

## 5. [신규] 위생 인증 뱃지 등록/삭제

식품안심업소·세스코 등 위생 인증 정보를 관리자가 등록합니다(점주는 조회만 가능).

### 5-1. 가게별 위생 뱃지 조회
```
GET /api/shops/v1/{id}/hygiene-badges
```
응답: `ShopHygieneBadgeResponse[]`
```jsonc
{
  "id": 7,
  "shopId": 5,
  "badgeType": "FOOD_SAFETY_CERTIFIED", // FOOD_SAFETY_CERTIFIED(식품안심업소)/CESCO_BLUE(블루세스코)/CESCO_WHITE(화이트세스코)
  "certifiedDate": "2026-03-01",         // 인증일
  "lastInspectionMonth": "2026-03"       // 세스코 최근 점검월(없으면 null)
}
```

### 5-2. 등록
```
POST /api/shops/v1/{id}/hygiene-badges
```
```jsonc
// ShopHygieneBadgeCreateRequest
{
  "badgeType": "CESCO_BLUE",
  "certifiedDate": "2026-03-01",
  "lastInspectionMonth": "2026-03"   // 선택(세스코만)
}
```

### 5-3. 삭제
```
DELETE /api/shops/v1/hygiene-badges/{hygieneBadgeId}
```
> **프론트 반영**: 가게 상세에 "위생 인증" 관리 탭 — 뱃지 유형 선택 + 인증일/점검월 입력 + 목록/삭제.

---

## 6. 신규 에러 코드(참고)
검수·상태 관련 응답에서 나올 수 있는 코드입니다. 사용자 메시지는 응답 본문의 메시지를 그대로 노출하면 됩니다.

| 코드 | HTTP | 의미 |
|---|---|---|
| `SHOP_IMAGE_CHANGE_REQUEST_NOT_FOUND` | 404 | 존재하지 않는 이미지 변경 요청 |
| `SHOP_IMAGE_CHANGE_REQUEST_NOT_PENDING` | 409 | 대기 상태가 아닌 요청을 승인/반려 |
| `SHOP_CONTENT_BOARD_NOT_FOUND` | 404 | 존재하지 않는 콘텐츠보드 |
| `SHOP_HYGIENE_BADGE_NOT_FOUND` | 404 | 존재하지 않는 위생 뱃지 |
| `HYGIENE_BADGE_TYPE_UNKNOWN` | 400 | 잘못된 위생 인증 유형 |
| `SHOP_BUSINESS_HOUR_INVALID_UNIT`/`_INVALID_RANGE` | 400 | 영업시간 규격 위반 |

---

## 7. 요약 체크리스트 (프론트 작업)
- [ ] 가게 등록 화면에 점주(ceoId) 선택 추가
- [ ] 영업시간 편집에 24시간 영업(is24Hours) 토글 + 응답 표시
- [ ] 이미지 검수 화면 신설(목록 필터·미리보기·승인/반려)
- [ ] 콘텐츠보드 숨김/삭제 조치 UI
- [ ] 위생 인증 뱃지 등록/조회/삭제 UI

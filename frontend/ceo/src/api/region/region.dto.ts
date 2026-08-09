// 행정동(지역) 검색 API 요청/응답 DTO (Region CEO — /api/admin-dongs)
// DTO 는 이 계층 밖으로 나가지 않는다. UI/feature 는 @/feature/shop/domain 을 사용한다.

export interface AdminDongSearchQueryRequest {
  /** 시/도·시군구·행정동명 부분 일치. 생략하면 전체 */
  keyword?: string;
}

export interface AdminDongItemResponse {
  id: number;
  /** 행정동 코드(10자리) */
  code: string;
  /** "서울특별시 강남구 역삼1동" 형태로 서버가 완성해서 내려준다 — 프론트가 조립하지 않는다 */
  regionName: string;
}

// ===== 행정동 계층(트리) 조회 =====

/**
 * 3단 lazy 조회의 요청 파라미터.
 *
 * 아무것도 없으면 시도 목록, `sidoName` 만 있으면 그 시도의 시군구 목록,
 * 둘 다 있으면 행정동 목록을 준다. `sigunguName` 만 단독으로 보내면 서버가 400 을 낸다.
 * 전 계층을 한 번에 받으면 3,600행이 넘으므로 단계별로 나눠 조회한다.
 */
export interface AdminDongTreeQueryRequest {
  sidoName?: string;
  sigunguName?: string;
}

export type AdminDongTreeLevelValue = "SIDO" | "SIGUNGU" | "DONG";

export interface AdminDongTreeItemResponse {
  /** 표시명 — 시도명·시군구명·행정동명 중 이 레벨에 해당하는 것 */
  name: string;
  /** `DONG` 레벨에서만 채워진다 */
  adminDongId: number | null;
  /** `DONG` 레벨에서만 채워진다 */
  code: string | null;
  /** 하위 행정동 수. `DONG` 레벨에서는 1 */
  dongCount: number;
}

export interface AdminDongTreeResponse {
  level: AdminDongTreeLevelValue;
  items: AdminDongTreeItemResponse[];
}

// ===== 행정동 경계 조회 =====

/**
 * 경계 조회 파라미터 — bbox 방식과 ID 지정 방식은 배타적이며 둘 다 없으면 서버가 400 을 낸다.
 *
 * `level` 은 지도 줌 레벨로, 서버가 이 값을 보고 정점 단순화 강도를 정한다.
 */
export interface AdminDongBoundaryQueryRequest {
  swLat?: number;
  swLng?: number;
  neLat?: number;
  neLng?: number;
  /** 1~14 */
  level: number;
  /** 콤마로 이어붙인 ID 목록. 최대 200개 */
  adminDongIds?: string;
}

/** 경계 좌표 한 점 — GeoJSON 의 `[lng, lat]` 순서 혼동을 없애려고 객체로 주고받는다 */
export interface GeoPointResponse {
  latitude: number;
  longitude: number;
}

export interface AdminDongBoundaryItemResponse {
  adminDongId: number;
  regionName: string;
  /** 대표점(경계 내부 보장점) */
  centerLatitude: number;
  centerLongitude: number;
  /** 경계 폴리곤. 경계 데이터를 아직 갖고 있지 않으면 404 가 아니라 null 로 내려온다 */
  rings: GeoPointResponse[][] | null;
}

export interface AdminDongBoundaryResponse {
  /** bbox 가 너무 넓어 목록을 생략했는지 — 이때 `items` 는 빈 배열이다 */
  truncated: boolean;
  items: AdminDongBoundaryItemResponse[];
}

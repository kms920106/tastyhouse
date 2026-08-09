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

/**
 * 평가 가능한 주문 메뉴 한 건.
 *
 * `orderProductId`가 키다 — 같은 상품을 2개 주문했으면 항목마다 다른 값을 갖고 각각 따로 평가된다.
 * `menuReviewId`가 null이 아니면 이미 평가한 항목이므로 수정(PUT) 대상이다.
 */
export interface MenuReviewWritableItem {
  orderProductId: number
  productId: number
  productName: string
  productImageUrl: string
  menuReviewId: number | null
  rating: number | null
  comment: string | null
}

/** 상품 상세에 노출되는 메뉴 평가 한 건. 댓글·좋아요·사장님답변이 없다. */
export interface MenuReviewListItem {
  id: number
  memberNickname: string
  memberProfileImageUrl: string
  rating: number
  comment: string | null
  createdAt: string
}

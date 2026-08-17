import type { PaginationParams } from '@/types/common'
import type { ReviewComment, ReviewReply } from './review.model'
import type { ReviewBlindReason, ReviewType } from './review.types'

export interface ReviewLatestQuery extends PaginationParams {
  type: ReviewType
}

export interface ReviewBestListItemResponse {
  id: number
  content: string
  imageUrl: string
  stationName: string
  shopName: string
  productName: string
  totalRating: number
}

export interface ReviewLatestListItemResponse {
  id: number
  imageUrls: string[]
  stationName: string
  totalRating: number
  content: string
  memberId: number
  memberNickname: string
  memberProfileImageUrl: string | null
  likeCount: number
  commentCount: number
  createdAt: string
}

export interface ReviewDetailResponse {
  id: number
  shopId: number
  shopName: string
  stationName: string
  content: string
  totalRating: number
  tasteRating: number
  amountRating: number
  priceRating: number
  atmosphereRating: number
  kindnessRating: number
  hygieneRating: number
  willRevisit: boolean
  memberId: number
  memberNickname: string
  memberProfileImageUrl: string | null
  createdAt: string
  imageUrls: string[]
  tagNames: string[]
  ownerOnly: boolean
  ownerReplyContent: string | null
  ownerReplyCreatedAt: string | null
  /**
   * 주문 유형. `DELIVERY`일 때만 수정 폼에 배달 평가 섹션을 렌더한다.
   * 작성자 본인이 아니면 서버가 null로 내린다.
   */
  orderMethod: string | null
  /**
   * 기존 배달 평가. 작성자 본인이 아니면 null.
   *
   * ⚠️ `PUT /api/reviews/v1/{reviewId}`는 전체 교체다 — 수정 폼은 이 값을 초기값으로 채워
   * 사용자가 건드리지 않았으면 그대로 되돌려 보내야 한다. 안 보내면 기존 배달 평가가 조용히 지워진다.
   */
  deliveryRating: number | null
  deliveryComment: string | null
}

export interface ReviewProductDetailResponse {
  productId: number
  productName: string
  productImageUrl: string
  productPrice: number
  content: string
  totalRating: number
  tasteRating: number
  amountRating: number
  priceRating: number
  atmosphereRating: number
  kindnessRating: number
  hygieneRating: number
  willRevisit: boolean
  memberId: number
  memberNickname: string
  memberProfileImageUrl: string | null
  createdAt: string
  imageUrls: string[]
  tagNames: string[]
}

export interface ReviewLikeResponse {
  liked: boolean
}

export interface CommentCreateRequest {
  content: string
}

export interface CommentCreateResponse {
  id: number
  reviewId: number
  memberId: number
  memberNickname: string
  memberProfileImageUrl: string | null
  content: string
  createdAt: string
  replies: ReviewReply[]
}

export interface CommentListResponse {
  comments: ReviewComment[]
  totalCount: number
}

export interface ReplyCreateRequest {
  content: string
  replyToMemberId: number
}

export interface ReplyCreateResponse {
  id: number
  commentId: number
  memberId: number
  memberNickname: string
  memberProfileImageUrl: string | null
  content: string
  createdAt: string
}

export interface ReviewWriteInfoResponse {
  productId: number
  productName: string
  productImageUrl: string
  productPrice: number
  orderId: number
  reviewed: boolean
  /** 배달 평가 섹션 렌더 판정용. `DELIVERY`일 때만 배달 평가를 받는다. */
  orderMethod: string
}

export interface ReviewCreateRequest {
  orderProductId: number | null
  productId: number
  tasteRating: number
  amountRating: number
  priceRating: number
  content: string
  uploadedFileIds: number[]
  tags: string[]
  ownerOnly: boolean
  /** 배달 주문에만 보낸다. 배달이 아닌데 값이 오면 서버가 400으로 거부한다. */
  deliveryRating?: number
  deliveryComment?: string
}

export interface ReviewCreateResponse {
  reviewId: number
  productId: number
  tasteRating: number
  amountRating: number
  priceRating: number
  totalRating: number
  content: string
  imageUrls: string[]
  tags: string[]
  createdAt: string
}

export interface ReviewUpdateRequest {
  tasteRating: number
  amountRating: number
  priceRating: number
  content: string
  uploadedFileIds: number[]
  tags: string[]
  /**
   * 배달 평가. 전체 교체(PUT)이므로 **기존 값을 그대로 되돌려 보내야 유지된다.**
   * `null`은 "지워줘"라는 뜻이다. 배달 주문이 아닌데 값이 오면 서버가 400으로 거부한다.
   */
  deliveryRating?: number | null
  deliveryComment?: string | null
}

export interface ReviewUpdateResponse {
  reviewId: number
  productId: number
  tasteRating: number
  amountRating: number
  priceRating: number
  totalRating: number
  content: string
  imageUrls: string[]
  tags: string[]
  createdAt: string
}

/**
 * `GET /api/reviews/v1/{reviewId}/blind` — 게시중단된 리뷰의 동의 화면 렌더용 조회.
 *
 * ⚠️ **`docs/tasks/backend.md` 4-2에는 이 조회 엔드포인트가 명시돼 있지 않다.**
 * 4-2는 consent/reject 두 mutation만 규정하는데, 3-2 화면은 "사유 · 재노출 예정일 · 리뷰 본문"을
 * 렌더해야 하고 기존 상세 조회(`GET /api/reviews/v1/{reviewId}`)는 `hidden.isFalse()` 필터에
 * 걸려 404다. 알림 `body` 문구만으로는 리뷰 본문을 얻을 수 없으므로 전용 조회가 필요하다고 보고
 * mutation 두 개와 같은 `/blind` 접두를 쓰는 `GET`을 가정한다.
 * 인가·404 규칙은 4-2와 동일하다고 전제한다(타인 리뷰 → 404 `REVIEW_NOT_FOUND`).
 */
export interface ReviewBlindDetailResponse {
  reviewId: number
  content: string
  imageUrls: string[]
  createdAt: string
  shopName: string
  /** 사장님이 선택한 게시중단 사유 코드 */
  reason: ReviewBlindReason
  /** 서버가 내려주는 사유 라벨. 없으면 `REVIEW_BLIND_REASON_LABEL`로 대체한다. */
  reasonDescription: string | null
  /** 상세 사유. `reason=ETC`가 아니면 null일 수 있다. */
  detailReason: string | null
  /** 재노출 예정일시. APPROVED 상태에서만 값이 있다. */
  blindUntil: string | null
}

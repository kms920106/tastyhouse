import 'server-only'

import { api, publicApi } from '@/lib/api'
import type { MyReviewListItemResponse } from '@/domains/member'
import { PaginationParams } from '@/types/common'
import {
  CommentCreateRequest,
  CommentCreateResponse,
  CommentListResponse,
  ReplyCreateRequest,
  ReplyCreateResponse,
  ReviewBestListItemResponse,
  ReviewBlindDetailResponse,
  ReviewCreateRequest,
  ReviewCreateResponse,
  ReviewDetailResponse,
  ReviewLatestListItemResponse,
  ReviewLatestQuery,
  ReviewLikeResponse,
  ReviewProductDetailResponse,
  ReviewUpdateRequest,
  ReviewUpdateResponse,
  ReviewWriteInfoResponse,
} from './review.dto'

const ENDPOINT = '/api/reviews'

// const CACHE_OPTIONS = { cache: 'force-cache' as const, next: { revalidate: 3600 } }

export const reviewRepository = {
  // 베스트 리뷰 목록 조회
  async getBestReviews(params: PaginationParams) {
    return publicApi.get<ReviewBestListItemResponse[]>(`${ENDPOINT}/v1/best`, { params })
  },
  // 최신 리뷰 목록 조회
  async getLatestReviews(params: ReviewLatestQuery) {
    return publicApi.get<ReviewLatestListItemResponse[]>(`${ENDPOINT}/v1/latest`, { params })
  },
  // 리뷰 상세 조회
  async getReviewDetail(reviewId: number) {
    return api.get<ReviewDetailResponse>(`${ENDPOINT}/v1/${reviewId}`)
  },
  // 리뷰 상세 정보 조회 (상품 정보 포함)
  async getReviewProductDetail(reviewId: number) {
    return api.get<ReviewProductDetailResponse>(`${ENDPOINT}/v1/${reviewId}/product`)
  },
  // 리뷰 좋아요 토글
  async toggleReviewLike(reviewId: number) {
    return api.post<ReviewLikeResponse>(`${ENDPOINT}/v1/${reviewId}/like`)
  },
  // 리뷰 좋아요 여부 조회
  async getReviewLike(reviewId: number) {
    return api.get<ReviewLikeResponse>(`${ENDPOINT}/v1/${reviewId}/like`)
  },
  // 댓글 등록
  async createReviewComment(reviewId: number, request: CommentCreateRequest) {
    return api.post<CommentCreateResponse>(`${ENDPOINT}/v1/${reviewId}/comments`, request)
  },
  // 댓글 및 답글 조회
  async getReviewComments(reviewId: number) {
    return api.get<CommentListResponse>(`${ENDPOINT}/v1/${reviewId}/comments`)
  },
  // 답글 등록
  async createReviewReply(reviewId: number, commentId: number, request: ReplyCreateRequest) {
    return api.post<ReplyCreateResponse>(`${ENDPOINT}/v1/comments/${commentId}/replies`, request)
  },
  // 리뷰 작성 정보 조회
  async getReviewWriteInfo(orderProductId: number) {
    return api.get<ReviewWriteInfoResponse>(`${ENDPOINT}/v1/write/order-items/${orderProductId}`)
  },
  // 리뷰 등록
  async createReview(request: ReviewCreateRequest) {
    return api.post<ReviewCreateResponse>(`${ENDPOINT}/v1`, request)
  },
  // 리뷰 수정
  async updateReview(reviewId: number, request: ReviewUpdateRequest) {
    return api.put<ReviewUpdateResponse>(`${ENDPOINT}/v1/${reviewId}`, request)
  },
  // 게시중단된 리뷰 조회 (삭제 동의 화면용 — 작성자 본인만 열람 가능)
  async getReviewBlindDetail(reviewId: number) {
    return api.get<ReviewBlindDetailResponse>(`${ENDPOINT}/v1/${reviewId}/blind`)
  },
  // 게시중단 리뷰 삭제 동의 — 리뷰가 즉시 삭제된다(되돌릴 수 없음)
  async consentReviewBlindDelete(reviewId: number) {
    return api.post<void>(`${ENDPOINT}/v1/${reviewId}/blind/consent`)
  },
  // 게시중단 리뷰 삭제 거부 — 상태 전이 없이 30일 배치가 재노출한다
  async rejectReviewBlindDelete(reviewId: number) {
    return api.post<void>(`${ENDPOINT}/v1/${reviewId}/blind/reject`)
  },
  // 특정 회원의 리뷰 목록 조회
  async getMemberReviews(memberId: number | string, params: PaginationParams) {
    return publicApi.get<MyReviewListItemResponse[]>(`${ENDPOINT}/v1/members/${memberId}`, {
      params,
    })
  },
}

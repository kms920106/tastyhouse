import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";
import type { ReviewComment, ReviewDetail, ReviewListItem } from "@/feature/review/domain";

import type { ReviewListQueryRequest } from "./review.dto";
import { reviewRepository } from "./review.repository";

export const reviewService = {
  // 리뷰 목록 조회 — 도메인 반환
  async getReviews(query: ReviewListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<ReviewListItem[]>> {
    const res = await reviewRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        shopId: item.shopId,
        productId: item.productId,
        memberId: item.memberId,
        memberNickname: item.memberNickname,
        totalRating: item.totalRating,
        content: item.content,
        hidden: item.hidden,
        createdAt: item.createdAt,
      })),
    };
  },

  // 리뷰 상세 조회 — 도메인 반환
  async getReview(id: number): Promise<ApiResponse<ReviewDetail>> {
    const res = await reviewRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        shopId: res.data.shopId,
        shopName: res.data.shopName,
        stationName: res.data.stationName,
        content: res.data.content,
        totalRating: res.data.totalRating,
        tasteRating: res.data.tasteRating,
        amountRating: res.data.amountRating,
        priceRating: res.data.priceRating,
        atmosphereRating: res.data.atmosphereRating,
        kindnessRating: res.data.kindnessRating,
        hygieneRating: res.data.hygieneRating,
        willRevisit: res.data.willRevisit,
        hidden: res.data.hidden,
        memberId: res.data.memberId,
        memberNickname: res.data.memberNickname,
        memberProfileImageUrl: res.data.memberProfileImageUrl,
        createdAt: res.data.createdAt,
        imageUrls: res.data.imageUrls,
        tagNames: res.data.tagNames,
      },
    };
  },

  // 리뷰 댓글/답글 조회 — 도메인 반환
  async getComments(id: number): Promise<ApiResponse<ReviewComment[]>> {
    const res = await reviewRepository.getComments(id);
    return {
      ...res,
      data: res.data?.map((comment) => ({
        id: comment.id,
        memberId: comment.memberId,
        memberNickname: comment.memberNickname,
        content: comment.content,
        hidden: comment.hidden,
        createdAt: comment.createdAt,
        replies: comment.replies.map((reply) => ({
          id: reply.id,
          memberId: reply.memberId,
          memberNickname: reply.memberNickname,
          replyToMemberId: reply.replyToMemberId,
          replyToMemberNickname: reply.replyToMemberNickname,
          content: reply.content,
          hidden: reply.hidden,
          createdAt: reply.createdAt,
        })),
      })),
    };
  },
};

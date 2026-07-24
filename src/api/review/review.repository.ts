import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  ReviewCommentListItemResponse,
  ReviewHiddenRequest,
  ReviewListItemResponse,
  ReviewListQueryRequest,
  ReviewManagementDetailResponse,
} from "./review.dto";

/**
 * 리뷰 관리자 API
 */

const ENDPOINT = "/api/reviews";

export const reviewRepository = {
  // 리뷰 목록 조회
  getList(query: ReviewListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<ReviewListItemResponse[]>> {
    return api.get<ReviewListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 리뷰 상세 조회
  getDetail(id: number): Promise<ApiResponse<ReviewManagementDetailResponse>> {
    return api.get<ReviewManagementDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // 리뷰 숨김/노출 전환
  updateHidden(id: number, body: ReviewHiddenRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${id}/hidden`, body);
  },

  // 리뷰 삭제 (이미지/태그 함께 삭제)
  remove(id: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${id}`);
  },

  // 리뷰 댓글/답글 조회 (숨김 처리된 항목 포함)
  getComments(id: number): Promise<ApiResponse<ReviewCommentListItemResponse[]>> {
    return api.get<ReviewCommentListItemResponse[]>(`${ENDPOINT}/v1/${id}/comments`);
  },

  // 댓글 숨김/노출 전환
  updateCommentHidden(commentId: number, body: ReviewHiddenRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/comments/${commentId}/hidden`, body);
  },

  // 댓글 삭제
  removeComment(commentId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/comments/${commentId}`);
  },

  // 답글 숨김/노출 전환
  updateReplyHidden(replyId: number, body: ReviewHiddenRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/replies/${replyId}/hidden`, body);
  },

  // 답글 삭제
  removeReply(replyId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/replies/${replyId}`);
  },
};

"use server";

import { revalidatePath } from "next/cache";

import { MAX_IMAGE_SIZE_BYTES } from "@/api/file/file.dto";
import { fileRepository } from "@/api/file/file.repository";
import { shopReviewRepository } from "@/api/shop-review/shop-review.repository";

import { BLIND_ATTACHMENT_ACCEPT, BLIND_ATTACHMENT_MAX_COUNT } from "./constants";
import {
  SHOP_REVIEW_COPY,
  SHOP_REVIEW_ERROR_MESSAGE,
  SHOP_REVIEW_MESSAGE,
  SHOP_REVIEW_VALIDATION_MESSAGE,
} from "./message";
import {
  blindRequestSchema,
  type OwnerReplyFormValues,
  ownerReplySchema,
  type SortTypeFormValues,
  sortTypeSchema,
} from "./schema";

const SHOP_REVIEW_PATH = "/dashboard/shop/reviews";

type ActionResult = {
  success: boolean;
  message?: string;
  id?: number;
};

/**
 * 서버 실패를 사용자 문구로 바꾼다.
 *
 * `errorCode` 표(`SHOP_REVIEW_ERROR_MESSAGE`)에 있으면 그 문구를, 없으면 호출부가 준
 * 기본 문구를 쓴다 — 서버 `message` 를 그대로 노출하지 않는 이유는 `message.ts` 주석 참고.
 */
function toFailure(errorCode: string | undefined, fallback: string): ActionResult {
  return { success: false, message: (errorCode && SHOP_REVIEW_ERROR_MESSAGE[errorCode]) ?? fallback };
}

function invalidInput(message?: string): ActionResult {
  return { success: false, message: message ?? SHOP_REVIEW_MESSAGE.INVALID_INPUT };
}

// ===== 앱 노출 정렬 설정 =====

export async function updateShopReviewSortTypeAction(
  shopId: number,
  values: SortTypeFormValues,
): Promise<ActionResult> {
  const parsed = sortTypeSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error, errorCode } = await shopReviewRepository.updateSortType(shopId, {
    sortType: parsed.data.sortType,
  });
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.SORT_TYPE_SAVE_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true };
}

// ===== 사장님 답변 =====

/**
 * 답변 등록.
 *
 * 클라이언트 검증만 믿지 않고 여기서도 스키마를 다시 태운다 — 서버 액션은 클라이언트를
 * 거치지 않고도 호출될 수 있으므로, 400 을 맞기 전에 같은 규칙으로 막는다.
 * 리뷰당 1건 제약은 서버 `UNIQUE(review_id)` 가 보증하므로 선판정하지 않고 409 를 문구로 갈라 낸다.
 */
export async function createOwnerReplyAction(
  shopId: number,
  reviewId: number,
  values: OwnerReplyFormValues,
): Promise<ActionResult> {
  const parsed = ownerReplySchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error, errorCode } = await shopReviewRepository.createOwnerReply(shopId, reviewId, {
    content: parsed.data.content,
  });
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.OWNER_REPLY_CREATE_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true, id: data ?? undefined };
}

export async function updateOwnerReplyAction(
  shopId: number,
  reviewId: number,
  values: OwnerReplyFormValues,
): Promise<ActionResult> {
  const parsed = ownerReplySchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error, errorCode } = await shopReviewRepository.updateOwnerReply(shopId, reviewId, {
    content: parsed.data.content,
  });
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.OWNER_REPLY_UPDATE_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true };
}

export async function deleteOwnerReplyAction(shopId: number, reviewId: number): Promise<ActionResult> {
  const { error, errorCode } = await shopReviewRepository.deleteOwnerReply(shopId, reviewId);
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.OWNER_REPLY_DELETE_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true };
}

// ===== 게시중단 요청 =====

/** 증빙 서류 허용 MIME — 클라이언트 `accept` 와 같은 목록을 서버 재검증에서도 쓴다 */
const BLIND_ATTACHMENT_ALLOWED_TYPES = BLIND_ATTACHMENT_ACCEPT.split(",");

/**
 * FormData 의 `attachments` 항목에서 업로드 가능한 파일만 꺼내 개수·형식·용량을 재검증한다.
 *
 * 클라이언트에서 `validateConsentFile()` 로 선검사하더라도 서버 액션은 클라이언트를 거치지 않고도
 * 호출될 수 있으므로 같은 규칙으로 다시 막는다(`feature/shop/actions.ts` 의 `extractConsentFile` 과 같은 형태).
 */
function extractAttachments(formData: FormData): { files: File[] } | { error: string } {
  const files = formData
    .getAll("attachments")
    .filter((entry): entry is File => entry instanceof File && entry.size > 0);

  if (files.length > BLIND_ATTACHMENT_MAX_COUNT) {
    return { error: SHOP_REVIEW_VALIDATION_MESSAGE.BLIND_ATTACHMENT_MAX_COUNT };
  }
  for (const file of files) {
    if (!BLIND_ATTACHMENT_ALLOWED_TYPES.includes(file.type)) {
      return { error: SHOP_REVIEW_VALIDATION_MESSAGE.BLIND_ATTACHMENT_TYPE };
    }
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      return { error: SHOP_REVIEW_VALIDATION_MESSAGE.BLIND_ATTACHMENT_SIZE };
    }
  }
  return { files };
}

/**
 * 게시중단 요청 등록 — 첨부 업로드(①) → 요청 생성(②) 2단 흐름.
 *
 * 신청 API 는 파일 원본이 아니라 `attachmentFileIds` 만 받으므로(`docs/tasks/backend.md` 4-1),
 * 먼저 `fileRepository.uploadImage()` 로 각 파일의 fileId 를 받아 배열로 모은 뒤 그것을 실어 보낸다.
 * **업로드가 이 액션 안에서 일어나야 하는 이유**는 `fileRepository` 가 `server-only` 라
 * 클라이언트 컴포넌트가 직접 호출할 수 없기 때문이다 — 시트는 `File[]` 을 FormData 로만 넘긴다.
 *
 * 업로드는 순차로 돌린다. 한 건이라도 실패하면 이미 올라간 파일은 고아로 남지만, 미참조 파일은
 * 요청에 실리지 않아 화면에 드러나지 않으므로 부분 실패를 되감지 않고 실패 문구만 돌려준다.
 *
 * 상세 사유가 빈 문자열이면 보내지 않는다 — 서버가 `ETC` 필수 판정을 할 때 빈 문자열과
 * 미전송을 다르게 볼 여지를 남기지 않기 위함이다.
 */
export async function createBlindRequestAction(
  shopId: number,
  reviewId: number,
  formData: FormData,
): Promise<ActionResult> {
  const parsed = blindRequestSchema.safeParse({
    reason: formData.get("reason"),
    detailReason: formData.get("detailReason") ?? undefined,
  } satisfies Record<string, unknown>);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const extracted = extractAttachments(formData);
  if ("error" in extracted) return { success: false, message: extracted.error };

  // ① 첨부 업로드 — fileId 수집
  const attachmentFileIds: number[] = [];
  for (const file of extracted.files) {
    const { data: fileId, error, errorCode } = await fileRepository.uploadImage(file);
    if (error !== undefined || fileId === undefined || fileId === null) {
      return toFailure(errorCode, SHOP_REVIEW_COPY.BLIND_ATTACHMENT_UPLOAD_FAILED);
    }
    attachmentFileIds.push(fileId);
  }

  // ② 요청 생성
  const detailReason = parsed.data.detailReason;
  const { data, error, errorCode } = await shopReviewRepository.createBlindRequest(shopId, reviewId, {
    reason: parsed.data.reason,
    ...(detailReason && detailReason.length > 0 ? { detailReason } : {}),
    ...(attachmentFileIds.length > 0 ? { attachmentFileIds } : {}),
  });
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.BLIND_REQUEST_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true, id: data ?? undefined };
}

/**
 * 게시중단 요청 취소.
 *
 * 취소 가능 조건(PENDING 만)은 서버 애그리거트의 불변식이라 여기서 선판정하지 않고,
 * 409 `REVIEW_BLIND_REQUEST_NOT_PENDING` 을 전용 문구로 갈라 낸다.
 */
export async function cancelBlindRequestAction(shopId: number, requestId: number): Promise<ActionResult> {
  const { error, errorCode } = await shopReviewRepository.cancelBlindRequest(shopId, requestId);
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.BLIND_CANCEL_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true };
}

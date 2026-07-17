"use server";

import { revalidatePath } from "next/cache";

import { partnershipRequestRepository } from "@/api/partnership-request/partnership-request.repository";
import { partnershipRequestService } from "@/api/partnership-request/partnership-request.service";
import type { PartnershipRequestDetail } from "@/feature/partnership-request/domain";

import { PARTNERSHIP_MESSAGE } from "./message";
import { type PartnershipStatusUpdateValues, partnershipStatusUpdateSchema } from "./schema";

const PARTNERSHIP_REQUESTS_PATH = "/dashboard/partnership-requests";

type ActionResult = {
  success: boolean;
  message?: string;
};

type PartnershipRequestDetailResult = {
  success: boolean;
  message?: string;
  data?: PartnershipRequestDetail;
};

// 제휴 신청 상세 조회
export async function fetchPartnershipRequestAction(id: number): Promise<PartnershipRequestDetailResult> {
  const { error, data } = await partnershipRequestService.getDetail(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 제휴 신청 처리 상태 변경
export async function updatePartnershipRequestStatusAction(
  id: number,
  values: PartnershipStatusUpdateValues,
): Promise<ActionResult> {
  const parsed = partnershipStatusUpdateSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? PARTNERSHIP_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await partnershipRequestRepository.updateStatus(id, { status: parsed.data.status });
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(PARTNERSHIP_REQUESTS_PATH);
  return { success: true };
}

// 제휴 신청 삭제 (Soft Delete)
export async function deletePartnershipRequestAction(id: number): Promise<ActionResult> {
  const { error } = await partnershipRequestRepository.remove(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(PARTNERSHIP_REQUESTS_PATH);
  return { success: true };
}

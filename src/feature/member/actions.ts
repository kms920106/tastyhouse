"use server";

import { revalidatePath } from "next/cache";

import { memberRepository } from "@/api/members/member.repository";
import { memberService } from "@/api/members/member.service";
import type { MemberDetail } from "@/feature/member/domain";

import { MEMBER_MESSAGE } from "./message";
import { type WithdrawalFormValues, withdrawalFormSchema } from "./schema";

const MEMBERS_PATH = "/dashboard/members";

type ActionResult = {
  success: boolean;
  message?: string;
};

type MemberDetailResult = {
  success: boolean;
  message?: string;
  data?: MemberDetail;
};

// 회원 상세 조회
export async function fetchMemberAction(id: number): Promise<MemberDetailResult> {
  const { error, data } = await memberService.getMember(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 회원 정지 (ACTIVE -> SUSPENDED)
export async function suspendMemberAction(id: number): Promise<ActionResult> {
  const { error } = await memberRepository.suspend(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(MEMBERS_PATH);
  return { success: true };
}

// 회원 정지 해제 (SUSPENDED -> ACTIVE)
export async function activateMemberAction(id: number): Promise<ActionResult> {
  const { error } = await memberRepository.activate(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(MEMBERS_PATH);
  return { success: true };
}

// 회원 강제 탈퇴 (-> DELETED)
export async function withdrawMemberAction(id: number, values: WithdrawalFormValues): Promise<ActionResult> {
  const parsed = withdrawalFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? MEMBER_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await memberRepository.withdraw(id, {
    reason: parsed.data.reason,
    reasonDetail: parsed.data.reasonDetail,
  });
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(MEMBERS_PATH);
  return { success: true };
}

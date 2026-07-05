"use server";

import { revalidatePath } from "next/cache";

import { noticeRepository } from "@/api/notice/notice.repository";
import { noticeService } from "@/api/notice/notice.service";
import type { NoticeDetail } from "@/feature/notice/domain";

import { NOTICE_MESSAGE } from "./message";
import { type NoticeFormValues, noticeFormSchema } from "./schema";

const NOTICES_PATH = "/dashboard/notices";

interface ActionResult {
  success: boolean;
  message?: string;
  id?: number;
}

interface NoticeDetailResult {
  success: boolean;
  message?: string;
  data?: NoticeDetail;
}

// 공지사항 등록
export async function createNoticeAction(values: NoticeFormValues): Promise<ActionResult> {
  const parsed = noticeFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? NOTICE_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await noticeRepository.create(parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(NOTICES_PATH);
  return { success: true, id: data };
}

// 공지사항 상세 조회
export async function fetchNoticeAction(id: number): Promise<NoticeDetailResult> {
  const { error, data } = await noticeService.getNotice(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 공지사항 수정
export async function updateNoticeAction(id: number, values: NoticeFormValues): Promise<ActionResult> {
  const parsed = noticeFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? NOTICE_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await noticeRepository.update(id, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(NOTICES_PATH);
  return { success: true };
}

// 공지사항 삭제
export async function deleteNoticeAction(id: number): Promise<ActionResult> {
  const { error } = await noticeRepository.remove(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(NOTICES_PATH);
  return { success: true };
}

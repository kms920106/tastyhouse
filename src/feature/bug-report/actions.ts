"use server";

import { revalidatePath } from "next/cache";

import { bugReportRepository } from "@/api/bug-report/bug-report.repository";
import { bugReportService } from "@/api/bug-report/bug-report.service";
import type { BugReportDetail } from "@/feature/bug-report/domain";

import { BUG_REPORT_MESSAGE } from "./message";
import {
  type AssignValues,
  assignSchema,
  type ClassifyValues,
  classifySchema,
  type StatusUpdateValues,
  statusUpdateSchema,
} from "./schema";

const BUG_REPORTS_PATH = "/dashboard/bug-reports";

type ActionResult = {
  success: boolean;
  message?: string;
};

type BugReportDetailResult = {
  success: boolean;
  message?: string;
  data?: BugReportDetail;
};

// 버그 제보 상세 조회
export async function fetchBugReportAction(id: number): Promise<BugReportDetailResult> {
  const { error, data } = await bugReportService.getBugReport(id);
  if (error !== undefined || !data) {
    return { success: false, message: error ?? BUG_REPORT_MESSAGE.DETAIL_LOAD_FAILED };
  }
  return { success: true, data };
}

// 버그 제보 처리 상태 변경
export async function updateBugReportStatusAction(id: number, values: StatusUpdateValues): Promise<ActionResult> {
  const parsed = statusUpdateSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? BUG_REPORT_MESSAGE.INVALID_INPUT };
  }

  const answer = parsed.data.answer?.trim();
  const { error } = await bugReportRepository.updateStatus(id, {
    status: parsed.data.status,
    answer: answer ? answer : null,
  });
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(BUG_REPORTS_PATH);
  return { success: true };
}

// 버그 제보 분류/우선순위 지정
export async function classifyBugReportAction(id: number, values: ClassifyValues): Promise<ActionResult> {
  const parsed = classifySchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? BUG_REPORT_MESSAGE.INVALID_INPUT };
  }

  const { error } = await bugReportRepository.classify(id, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(BUG_REPORTS_PATH);
  return { success: true };
}

// 버그 제보 담당자 배정
export async function assignBugReportAction(id: number, values: AssignValues): Promise<ActionResult> {
  const parsed = assignSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? BUG_REPORT_MESSAGE.INVALID_INPUT };
  }

  const { error } = await bugReportRepository.assign(id, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(BUG_REPORTS_PATH);
  return { success: true };
}

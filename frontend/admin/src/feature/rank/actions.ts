"use server";

import { revalidatePath } from "next/cache";

import { fileRepository } from "@/api/file/file.repository";
import type { RankMemberListQueryRequest } from "@/api/rank/rank.dto";
import { rankRepository } from "@/api/rank/rank.repository";
import { rankService } from "@/api/rank/rank.service";
import type { RankMember, RankPeriodDetail, RankPrize, RankPrizeDetail } from "@/feature/rank/domain";

import { RANK_MESSAGE } from "./message";
import {
  type AggregationFormValues,
  aggregationSchema,
  type PeriodFormValues,
  type PrizeFormValues,
  periodSchema,
  prizeSchema,
  toApiDateTime,
} from "./schema";

const RANKS_PATH = "/dashboard/ranks";

type ActionResult = {
  success: boolean;
  message?: string;
  id?: number;
};

type MembersResult = {
  success: boolean;
  message?: string;
  data?: RankMember[];
};

type PeriodDetailResult = {
  success: boolean;
  message?: string;
  data?: RankPeriodDetail;
};

type PrizesResult = {
  success: boolean;
  message?: string;
  data?: RankPrize[];
};

type PrizeDetailResult = {
  success: boolean;
  message?: string;
  data?: RankPrizeDetail;
};

type ImageUploadResult = {
  success: boolean;
  message?: string;
  /** 업로드된 파일의 ID(fileId) */
  fileId?: number;
};

function toPeriodBody(values: PeriodFormValues) {
  return {
    startAt: toApiDateTime(values.startAt),
    endAt: toApiDateTime(values.endAt),
    visible: values.visible,
  };
}

// 회원 랭킹 목록 조회
export async function fetchRankMembersAction(query: RankMemberListQueryRequest): Promise<MembersResult> {
  const { error, data } = await rankService.getMembers(query);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 랭킹 집계 수동 실행
export async function runAggregationAction(values: AggregationFormValues): Promise<ActionResult> {
  const parsed = aggregationSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? RANK_MESSAGE.INVALID_INPUT,
    };
  }

  const { type, baseDate, limit } = parsed.data;
  const { error } = await rankRepository.aggregate(type === undefined ? {} : { type, baseDate, limit });
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(RANKS_PATH);
  return { success: true };
}

// 랭킹 기간 목록 조회 (드롭다운/시트 재조회용)
export async function fetchPeriodsAction() {
  const { error, data } = await rankService.getPeriods();
  if (error !== undefined) {
    return { success: false as const, message: error };
  }
  return { success: true as const, data };
}

// 랭킹 기간 상세 조회
export async function fetchPeriodAction(id: number): Promise<PeriodDetailResult> {
  const { error, data } = await rankService.getPeriod(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 랭킹 기간 등록
export async function createPeriodAction(values: PeriodFormValues): Promise<ActionResult> {
  const parsed = periodSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? RANK_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await rankRepository.createPeriod(toPeriodBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(RANKS_PATH);
  return { success: true, id: data };
}

// 랭킹 기간 수정
export async function updatePeriodAction(id: number, values: PeriodFormValues): Promise<ActionResult> {
  const parsed = periodSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? RANK_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await rankRepository.updatePeriod(id, toPeriodBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(RANKS_PATH);
  return { success: true };
}

// 랭킹 기간 삭제 (하위 경품도 함께 삭제됨)
export async function deletePeriodAction(id: number): Promise<ActionResult> {
  const { error } = await rankRepository.deletePeriod(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(RANKS_PATH);
  return { success: true };
}

// 특정 기간의 경품 목록 조회
export async function fetchPrizesAction(periodId: number): Promise<PrizesResult> {
  const { error, data } = await rankService.getPrizes(periodId);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 경품 단건 상세 조회
export async function fetchPrizeAction(prizeId: number): Promise<PrizeDetailResult> {
  const { error, data } = await rankService.getPrize(prizeId);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 경품 등록 (기간 ID 기준)
export async function createPrizeAction(periodId: number, values: PrizeFormValues): Promise<ActionResult> {
  const parsed = prizeSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? RANK_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await rankRepository.createPrize(periodId, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  // 경품은 시트 내부에서 재조회하므로 revalidatePath 불필요.
  return { success: true, id: data };
}

// 경품 수정
export async function updatePrizeAction(prizeId: number, values: PrizeFormValues): Promise<ActionResult> {
  const parsed = prizeSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? RANK_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await rankRepository.updatePrize(prizeId, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  return { success: true };
}

// 경품 삭제
export async function deletePrizeAction(prizeId: number): Promise<ActionResult> {
  const { error } = await rankRepository.deletePrize(prizeId);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  return { success: true };
}

// 경품 이미지 업로드 (파일 → fileId)
export async function uploadPrizeImageAction(formData: FormData): Promise<ImageUploadResult> {
  const file = formData.get("file");
  if (!(file instanceof File)) {
    return { success: false, message: RANK_MESSAGE.IMAGE_UPLOAD_FAILED };
  }

  const { error, data } = await fileRepository.uploadImage(file);
  if (error !== undefined || data == null) {
    return { success: false, message: error ?? RANK_MESSAGE.IMAGE_UPLOAD_FAILED };
  }

  return { success: true, fileId: data };
}

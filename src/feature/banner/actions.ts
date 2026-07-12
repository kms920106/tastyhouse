"use server";

import { revalidatePath } from "next/cache";

import { bannerRepository } from "@/api/banner/banner.repository";
import { bannerService } from "@/api/banner/banner.service";
import { imageRepository } from "@/api/banner/image.repository";
import type { BannerDetail, BannerImage } from "@/feature/banner/domain";

import { BANNER_MESSAGE } from "./message";
import { type BannerFormValues, bannerFormSchema } from "./schema";

const BANNERS_PATH = "/dashboard/banners";

type ActionResult = {
  success: boolean;
  message?: string;
  id?: number;
};

type BannerDetailResult = {
  success: boolean;
  message?: string;
  data?: BannerDetail;
};

type ImageUploadResult = {
  success: boolean;
  message?: string;
  data?: BannerImage;
};

/** "YYYY-MM-DDTHH:mm" (datetime-local) -> "YYYY-MM-DDTHH:mm:ss" (LocalDateTime) */
function toLocalDateTime(value: string | undefined): string | undefined {
  if (!value) return undefined;
  return value.length === 16 ? `${value}:00` : value;
}

function toRequestBody(values: BannerFormValues) {
  return {
    type: values.type,
    title: values.title,
    imageFileId: values.imageFileId,
    linkUrl: values.linkUrl,
    startDate: toLocalDateTime(values.startDate),
    endDate: toLocalDateTime(values.endDate),
    sort: values.sort,
    visible: values.visible,
  };
}

// 배너 이미지 업로드
export async function uploadBannerImageAction(formData: FormData): Promise<ImageUploadResult> {
  const file = formData.get("file");
  if (!(file instanceof File)) {
    return { success: false, message: BANNER_MESSAGE.IMAGE_UPLOAD_FAILED };
  }

  const { error, data } = await imageRepository.upload(file);
  if (error !== undefined || !data) {
    return { success: false, message: error ?? BANNER_MESSAGE.IMAGE_UPLOAD_FAILED };
  }

  return { success: true, data };
}

// 배너 등록
export async function createBannerAction(values: BannerFormValues): Promise<ActionResult> {
  const parsed = bannerFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? BANNER_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await bannerRepository.create(toRequestBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(BANNERS_PATH);
  return { success: true, id: data };
}

// 배너 상세 조회
export async function fetchBannerAction(id: number): Promise<BannerDetailResult> {
  const { error, data } = await bannerService.getBanner(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 배너 수정
export async function updateBannerAction(id: number, values: BannerFormValues): Promise<ActionResult> {
  const parsed = bannerFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? BANNER_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await bannerRepository.update(id, toRequestBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(BANNERS_PATH);
  return { success: true };
}

// 배너 삭제
export async function deleteBannerAction(id: number): Promise<ActionResult> {
  const { error } = await bannerRepository.remove(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(BANNERS_PATH);
  return { success: true };
}

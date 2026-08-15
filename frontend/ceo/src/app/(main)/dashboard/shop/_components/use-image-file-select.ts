"use client";

import * as React from "react";

import { MAX_IMAGE_SIZE_BYTES } from "@/api/file/file.dto";
import {
  ALLOWED_CONSENT_TYPES,
  CONTENT_BOARD_GIF_MIN_SIZE,
  CONTENT_BOARD_IMAGE_MAX_BYTES,
  CONTENT_BOARD_IMAGE_MIN_SIZE,
  THUMBNAIL_IMAGE_MAX_BYTES,
  THUMBNAIL_IMAGE_MIN_SIZE,
  TRADEMARK_IMAGE_MAX_BYTES,
  TRADEMARK_IMAGE_MIN_SIZE,
} from "@/feature/shop/constants";
import { SHOP_MESSAGE } from "@/feature/shop/message";
import {
  NOTICE_IMAGE_MAX_BYTES,
  NOTICE_IMAGE_MIN_HEIGHT,
  NOTICE_IMAGE_MIN_WIDTH,
} from "@/feature/shop-notice/constants";
import { SHOP_NOTICE_MESSAGE } from "@/feature/shop-notice/message";

/** 공지 이미지 허용 MIME. `NOTICE_IMAGE_ACCEPT` 와 같은 목록이지만 검증은 배열로 다룬다 */
const NOTICE_IMAGE_ALLOWED_TYPES = ["image/jpeg", "image/png"] as const;

export type ImageValidationKind = "trademark" | "thumbnail" | "contentBoardImage" | "contentBoardGif" | "noticeImage";

/**
 * 동의서 첨부 검증 — 이미지 스캔본과 PDF 를 모두 허용하며, 치수 검증은 하지 않는다.
 *
 * PDF 는 createImageBitmap 으로 열리지 않으므로 validateImageFile 을 재사용할 수 없다.
 * 서버 액션(extractConsentFile)에서 MIME/용량을 다시 검증하므로 여기서의 검사는 사용자 피드백 목적이다.
 */
export function validateConsentFile(file: File): string | null {
  if (!ALLOWED_CONSENT_TYPES.includes(file.type as (typeof ALLOWED_CONSENT_TYPES)[number])) {
    return SHOP_MESSAGE.CONSENT_FILE_TYPE;
  }
  if (file.size > MAX_IMAGE_SIZE_BYTES) return SHOP_MESSAGE.CONSENT_FILE_SIZE;
  return null;
}

/**
 * 규격 위반 이미지는 전송 전에 걸러야 하므로 createImageBitmap 으로 실제 픽셀 크기를 먼저 읽는다.
 * 서버 액션(extractFile)에서 MIME/용량을 다시 검증하므로 여기서의 검사는 사용자 피드백 목적이다.
 */
export async function validateImageFile(file: File, kind: ImageValidationKind): Promise<string | null> {
  if (kind === "trademark") {
    if (file.type !== "image/jpeg") return SHOP_MESSAGE.TRADEMARK_IMAGE_TYPE;
    if (file.size > TRADEMARK_IMAGE_MAX_BYTES) return SHOP_MESSAGE.TRADEMARK_IMAGE_SIZE;
  }
  if (kind === "thumbnail" && file.size > THUMBNAIL_IMAGE_MAX_BYTES) {
    return SHOP_MESSAGE.THUMBNAIL_IMAGE_SIZE;
  }
  if ((kind === "contentBoardImage" || kind === "contentBoardGif") && file.size > CONTENT_BOARD_IMAGE_MAX_BYTES) {
    return SHOP_MESSAGE.UPLOAD_FAILED;
  }
  if (kind === "noticeImage") {
    if (!NOTICE_IMAGE_ALLOWED_TYPES.includes(file.type as (typeof NOTICE_IMAGE_ALLOWED_TYPES)[number])) {
      return SHOP_NOTICE_MESSAGE.IMAGE_SPEC_INVALID;
    }
    if (file.size > NOTICE_IMAGE_MAX_BYTES) return SHOP_NOTICE_MESSAGE.IMAGE_SPEC_INVALID;
  }

  let bitmap: ImageBitmap;
  try {
    bitmap = await createImageBitmap(file);
  } catch {
    return SHOP_MESSAGE.UPLOAD_FAILED;
  }

  const { width, height } = bitmap;
  bitmap.close();

  switch (kind) {
    case "trademark":
      // 상표는 1:1 정사각형 필수
      if (width !== height || width < TRADEMARK_IMAGE_MIN_SIZE) return SHOP_MESSAGE.TRADEMARK_IMAGE_DIMENSION;
      return null;
    case "thumbnail":
      if (width < THUMBNAIL_IMAGE_MIN_SIZE || height < THUMBNAIL_IMAGE_MIN_SIZE) {
        return SHOP_MESSAGE.THUMBNAIL_IMAGE_DIMENSION;
      }
      return null;
    case "contentBoardGif":
      if (width < CONTENT_BOARD_GIF_MIN_SIZE || height < CONTENT_BOARD_GIF_MIN_SIZE) {
        return SHOP_MESSAGE.CONTENT_BOARD_GIF_DIMENSION;
      }
      return null;
    case "contentBoardImage":
      if (width < CONTENT_BOARD_IMAGE_MIN_SIZE || height < CONTENT_BOARD_IMAGE_MIN_SIZE) {
        return SHOP_MESSAGE.CONTENT_BOARD_IMAGE_DIMENSION;
      }
      return null;
    case "noticeImage":
      // 공지는 가로로 긴 배너 형태라 정사각 하한이 아니라 가로·세로를 따로 본다.
      if (width < NOTICE_IMAGE_MIN_WIDTH || height < NOTICE_IMAGE_MIN_HEIGHT) {
        return SHOP_NOTICE_MESSAGE.IMAGE_SPEC_INVALID;
      }
      return null;
  }
}

/**
 * 파일 선택 → 규격 검증 → 원본 File 보관.
 * 스펙이 multipart 직접 전송으로 바뀌어 별도 선업로드(fileId 발급) 단계는 없다.
 */
export function useImageFileSelect(kind: ImageValidationKind) {
  const [isValidating, setIsValidating] = React.useState(false);

  const select = React.useCallback(
    async (file: File): Promise<{ file: File } | { error: string }> => {
      setIsValidating(true);
      const error = await validateImageFile(file, kind);
      setIsValidating(false);
      return error ? { error } : { file };
    },
    [kind],
  );

  return { select, isValidating };
}

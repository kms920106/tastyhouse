// 파일 업로드 API 요청/응답 DTO (File Admin — /api/files)

/** 허용 이미지 확장자 (jpg, png, gif, webp) */
export const ALLOWED_IMAGE_TYPES = ["image/jpeg", "image/png", "image/gif", "image/webp"] as const;

/** 최대 업로드 크기 (10MB) */
export const MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;

/**
 * 이미지 업로드 성공 응답의 data 필드.
 * 백엔드는 업로드된 파일의 ID(fileId)만 Long 으로 반환한다.
 */
export type FileUploadResponse = number;

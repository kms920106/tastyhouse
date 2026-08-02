import "server-only";

import { api } from "@/api/shared/client";
import type { ApiResponse } from "@/api/shared/types";

import type { FileUploadResponse } from "./file.dto";

/**
 * File Admin API (파일 업로드 관리자 API)
 * Base URL: /api/files
 */
const FILE_UPLOAD_ENDPOINT = "/api/files/v1/upload";

export const fileRepository = {
  /**
   * 이미지 파일 업로드 (jpg, png, gif, webp / 최대 10MB)
   * @returns 업로드된 파일의 ID(fileId)
   */
  uploadImage(file: File): Promise<ApiResponse<FileUploadResponse>> {
    const formData = new FormData();
    formData.append("file", file);
    return api.upload<FileUploadResponse>(FILE_UPLOAD_ENDPOINT, formData);
  },
};

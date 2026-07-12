import "server-only";

import { api } from "@/api/shared/client";
import type { ApiResponse } from "@/api/shared/types";

import type { FileResponse } from "./image.dto";

/**
 * 이미지 파일 업로드 API
 * NOTE: 업로드 API 엔드포인트가 아직 확정되지 않아 임의 경로를 사용한다.
 * 응답 계약(FileResponse)은 백엔드 공용 FileResponse 로 확정됨.
 * 실제 엔드포인트 확정 시 이 파일의 UPLOAD_ENDPOINT만 교체하면 된다.
 */

const UPLOAD_ENDPOINT = "/api/files/v1";

export const imageRepository = {
  upload(file: File): Promise<ApiResponse<FileResponse>> {
    const formData = new FormData();
    formData.append("file", file);
    return api.upload<FileResponse>(UPLOAD_ENDPOINT, formData);
  },
};

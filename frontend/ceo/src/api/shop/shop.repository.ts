import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  AmenityCategoryResponse,
  AmenityCreateRequest,
  BreakTimeCreateRequest,
  BreakTimeResponse,
  BreakTimeUpdateRequest,
  BusinessHourCreateRequest,
  BusinessHourResponse,
  BusinessHourUpdateRequest,
  ClosedDayCreateRequest,
  ContentBoardMutationFields,
  ContentBoardResponse,
  HolidayClosedUpdateRequest,
  HygieneBadgeResponse,
  PhoneNumberCreateRequest,
  PhoneNumberResponse,
  ShopAmenityResponse,
  ShopClosedDaysResponse,
  ShopConvenienceInfoResponse,
  ShopConvenienceInfoUpdateRequest,
  ShopDetailResponse,
  ShopImageStatusResponse,
  ShopIntroductionResponse,
  ShopIntroductionUpdateRequest,
  ShopIntroductionValidateResponse,
  ShopListItemResponse,
  ShopListQueryRequest,
  ShopStatusResponse,
  ShopStatusUpdateRequest,
  SuspensionBulkCreateRequest,
  SuspensionCreateRequest,
  SuspensionResponse,
  TemporaryClosureCreateRequest,
} from "./shop.dto";

/**
 * 점주용 가게 관리 API
 *
 * 하위 리소스 경로 규칙은 스펙마다 다르다 — 절대 규칙으로 취급하지 말 것.
 * business-hours/break-times/closed-days/phone-numbers 는 수정·삭제가 하위 리소스
 * 자신의 ID 경로를 쓰지만(`/v1/business-hours/{id}`), content-boards 와 suspensions 는
 * 부모 `{shopId}` 경로를 그대로 유지한다(`/v1/{shopId}/content-boards/{id}`,
 * `/v1/{shopId}/suspensions`). 스펙 문서(docs/CEO-API-SHOP-SPEC-FOR-FRONTEND.md)를
 * 그대로 따를 것.
 */

const ENDPOINT = "/api/shops";

function toContentBoardFormData(body: ContentBoardMutationFields, file?: File): FormData {
  const formData = new FormData();
  formData.append("contentType", body.contentType);
  formData.append("topic", body.topic);
  formData.append("description", body.description);
  if (body.youtubeUrl) formData.append("youtubeUrl", body.youtubeUrl);
  if (file) formData.append("file", file);
  return formData;
}

export const shopRepository = {
  // ===== 내 가게 조회 =====

  getList(query: ShopListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<ShopListItemResponse[]>> {
    return api.get<ShopListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  getDetail(shopId: number): Promise<ApiResponse<ShopDetailResponse>> {
    return api.get<ShopDetailResponse>(`${ENDPOINT}/v1/${shopId}`);
  },

  // ===== 영업시간 · 휴게시간 =====

  getBusinessHours(shopId: number): Promise<ApiResponse<BusinessHourResponse[]>> {
    return api.get<BusinessHourResponse[]>(`${ENDPOINT}/v1/${shopId}/business-hours`);
  },

  createBusinessHour(shopId: number, body: BusinessHourCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/business-hours`, body);
  },

  // 경로가 shopId 가 아닌 businessHourId 기준임에 주의
  updateBusinessHour(businessHourId: number, body: BusinessHourUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/business-hours/${businessHourId}`, body);
  },

  // 경로가 shopId 가 아닌 businessHourId 기준임에 주의
  deleteBusinessHour(businessHourId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/business-hours/${businessHourId}`);
  },

  getBreakTimes(shopId: number): Promise<ApiResponse<BreakTimeResponse[]>> {
    return api.get<BreakTimeResponse[]>(`${ENDPOINT}/v1/${shopId}/break-times`);
  },

  createBreakTime(shopId: number, body: BreakTimeCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/break-times`, body);
  },

  // 경로가 shopId 가 아닌 breakTimeId 기준임에 주의
  updateBreakTime(breakTimeId: number, body: BreakTimeUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/break-times/${breakTimeId}`, body);
  },

  // 경로가 shopId 가 아닌 breakTimeId 기준임에 주의
  deleteBreakTime(breakTimeId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/break-times/${breakTimeId}`);
  },

  // ===== 휴무일 (공휴일 · 정기 · 임시 통합) =====

  getClosedDays(shopId: number): Promise<ApiResponse<ShopClosedDaysResponse>> {
    return api.get<ShopClosedDaysResponse>(`${ENDPOINT}/v1/${shopId}/closed-days`);
  },

  updateHolidayClosed(shopId: number, body: HolidayClosedUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${shopId}/closed-days/holiday`, body);
  },

  createClosedDay(shopId: number, body: ClosedDayCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/closed-days`, body);
  },

  // 경로가 shopId 가 아닌 closedDayId 기준임에 주의
  deleteClosedDay(closedDayId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/closed-days/${closedDayId}`);
  },

  createTemporaryClosure(shopId: number, body: TemporaryClosureCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/temporary-closures`, body);
  },

  // 경로가 shopId 가 아닌 temporaryClosureId 기준임에 주의
  deleteTemporaryClosure(temporaryClosureId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/temporary-closures/${temporaryClosureId}`);
  },

  // ===== 가게 전화번호 (다건 + 대표번호) =====

  getPhoneNumbers(shopId: number): Promise<ApiResponse<PhoneNumberResponse[]>> {
    return api.get<PhoneNumberResponse[]>(`${ENDPOINT}/v1/${shopId}/phone-numbers`);
  },

  createPhoneNumber(shopId: number, body: PhoneNumberCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/phone-numbers`, body);
  },

  // 경로가 shopId 가 아닌 phoneNumberId 기준임에 주의
  deletePhoneNumber(phoneNumberId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/phone-numbers/${phoneNumberId}`);
  },

  // 대표번호 지정 — 경로가 shopId 가 아닌 phoneNumberId 기준임에 주의
  setPrimaryPhoneNumber(phoneNumberId: number): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/phone-numbers/${phoneNumberId}/primary`);
  },

  // ===== 가게 상태 =====

  getStatus(shopId: number): Promise<ApiResponse<ShopStatusResponse>> {
    return api.get<ShopStatusResponse>(`${ENDPOINT}/v1/${shopId}/status`);
  },

  updateStatus(shopId: number, body: ShopStatusUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${shopId}/status`, body);
  },

  // ===== 가게 소개 =====

  getIntroduction(shopId: number): Promise<ApiResponse<ShopIntroductionResponse>> {
    return api.get<ShopIntroductionResponse>(`${ENDPOINT}/v1/${shopId}/introduction`);
  },

  updateIntroduction(shopId: number, body: ShopIntroductionUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${shopId}/introduction`, body);
  },

  validateIntroduction(
    shopId: number,
    body: ShopIntroductionUpdateRequest,
  ): Promise<ApiResponse<ShopIntroductionValidateResponse>> {
    return api.post<ShopIntroductionValidateResponse>(`${ENDPOINT}/v1/${shopId}/introduction/validate`, body);
  },

  // ===== 편의정보 · 찾아오는 길 · 노출 위치 =====

  getConvenienceInfo(shopId: number): Promise<ApiResponse<ShopConvenienceInfoResponse>> {
    return api.get<ShopConvenienceInfoResponse>(`${ENDPOINT}/v1/${shopId}/convenience-info`);
  },

  updateConvenienceInfo(shopId: number, body: ShopConvenienceInfoUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${shopId}/convenience-info`, body);
  },

  // ===== 편의시설(기타) =====

  getAmenityCategories(): Promise<ApiResponse<AmenityCategoryResponse[]>> {
    return api.get<AmenityCategoryResponse[]>(`${ENDPOINT}/v1/amenity-categories`);
  },

  getAmenities(shopId: number): Promise<ApiResponse<ShopAmenityResponse[]>> {
    return api.get<ShopAmenityResponse[]>(`${ENDPOINT}/v1/${shopId}/amenities`);
  },

  createAmenity(shopId: number, body: AmenityCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/amenities`, body);
  },

  // 경로가 amenityCategoryId 기준 — 하위 리소스 자신의 ID 가 아니라 스펙이 지정한 카테고리 ID 를 그대로 씀
  deleteAmenity(shopId: number, amenityCategoryId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${shopId}/amenities/${amenityCategoryId}`);
  },

  // ===== 상표 · 대표이미지 (승인 워크플로) =====

  getTrademark(shopId: number): Promise<ApiResponse<ShopImageStatusResponse>> {
    return api.get<ShopImageStatusResponse>(`${ENDPOINT}/v1/${shopId}/trademark`);
  },

  createTrademarkRequest(shopId: number, file: File): Promise<ApiResponse<null>> {
    const formData = new FormData();
    formData.append("file", file);
    return api.upload<null>(`${ENDPOINT}/v1/${shopId}/trademark/requests`, formData);
  },

  getThumbnail(shopId: number): Promise<ApiResponse<ShopImageStatusResponse>> {
    return api.get<ShopImageStatusResponse>(`${ENDPOINT}/v1/${shopId}/thumbnail`);
  },

  createThumbnailRequest(shopId: number, file: File): Promise<ApiResponse<null>> {
    const formData = new FormData();
    formData.append("file", file);
    return api.upload<null>(`${ENDPOINT}/v1/${shopId}/thumbnail/requests`, formData);
  },

  // ===== 콘텐츠보드 =====

  getContentBoards(shopId: number): Promise<ApiResponse<ContentBoardResponse[]>> {
    return api.get<ContentBoardResponse[]>(`${ENDPOINT}/v1/${shopId}/content-boards`);
  },

  createContentBoard(shopId: number, fields: ContentBoardMutationFields, file?: File): Promise<ApiResponse<number>> {
    return api.upload<number>(`${ENDPOINT}/v1/${shopId}/content-boards`, toContentBoardFormData(fields, file));
  },

  // 경로는 부모 shopId 하위를 그대로 유지한다(스펙 §8) — multipart PUT
  updateContentBoard(
    shopId: number,
    contentBoardId: number,
    fields: ContentBoardMutationFields,
    file?: File,
  ): Promise<ApiResponse<null>> {
    return api.uploadPut<null>(
      `${ENDPOINT}/v1/${shopId}/content-boards/${contentBoardId}`,
      toContentBoardFormData(fields, file),
    );
  },

  // 경로는 부모 shopId 하위를 그대로 유지한다(스펙 §8)
  deleteContentBoard(shopId: number, contentBoardId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${shopId}/content-boards/${contentBoardId}`);
  },

  // ===== 영업 임시중지 ('준비중') =====

  getSuspensions(shopId: number): Promise<ApiResponse<SuspensionResponse[]>> {
    return api.get<SuspensionResponse[]>(`${ENDPOINT}/v1/${shopId}/suspensions`);
  },

  // 단건 등록 — 경로가 shopId 하위를 그대로 유지한다(스펙 §9)
  createSuspension(shopId: number, body: SuspensionCreateRequest): Promise<ApiResponse<SuspensionResponse[]>> {
    return api.post<SuspensionResponse[]>(`${ENDPOINT}/v1/${shopId}/suspensions`, body);
  },

  // 일괄 등록 — 여러 가게를 한 번에 처리하므로 shopId 하위 경로가 아니다
  createSuspensionsBulk(body: SuspensionBulkCreateRequest): Promise<ApiResponse<null>> {
    return api.post<null>(`${ENDPOINT}/v1/suspensions/bulk`, body);
  },

  // 해제 — 경로가 shopId 가 아닌 suspensionId 기준임에 주의
  releaseSuspension(shopId: number, suspensionId: number): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/${shopId}/suspensions/${suspensionId}/release`);
  },

  // ===== 위생 인증 (조회 전용) =====

  getHygieneBadges(shopId: number): Promise<ApiResponse<HygieneBadgeResponse[]>> {
    return api.get<HygieneBadgeResponse[]>(`${ENDPOINT}/v1/${shopId}/hygiene-badges`);
  },
};

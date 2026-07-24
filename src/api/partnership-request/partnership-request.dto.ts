// 처리 상태: PENDING(접수 대기) / IN_PROGRESS(처리 중) / COMPLETED(처리 완료)
export type PartnershipStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED";

// 제휴 신청 목록 조회
export interface PartnershipRequestListQueryRequest {
  businessName?: string;
  contactName?: string;
  contactPhone?: string;
  status?: PartnershipStatus;
  startDate?: string;
  endDate?: string;
}

// 제휴 신청 목록 조회
export interface PartnershipRequestListItemResponse {
  id: number;
  businessName: string;
  contactName: string;
  contactPhone: string;
  status: PartnershipStatus;
  consultationRequestedAt: string;
  createdAt: string;
}

// 제휴 신청 상세 조회
export interface PartnershipRequestDetailResponse {
  id: number;
  businessName: string;
  address: string;
  addressDetail: string;
  contactName: string;
  contactPhone: string;
  status: PartnershipStatus;
  consultationRequestedAt: string;
  createdAt: string;
  updatedAt: string;
}

// 제휴 신청 처리 상태 변경
export interface PartnershipRequestStatusUpdateRequest {
  status: PartnershipStatus;
}

// 처리 상태: PENDING(접수 대기) / IN_PROGRESS(처리 중) / COMPLETED(처리 완료)
export type PartnershipStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED";

export interface PartnershipRequestListItem {
  id: number;
  businessName: string;
  contactName: string;
  contactPhone: string;
  status: PartnershipStatus;
  consultationRequestedAt: string;
  createdAt: string;
}

export interface PartnershipRequestDetail {
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

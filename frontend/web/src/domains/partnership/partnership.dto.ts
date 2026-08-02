export interface PartnershipRequestCreateRequest {
  businessName: string
  address: string
  addressDetail: string
  contactName: string
  contactPhone: string
  consultationRequestedAt: string
}

export interface PartnershipRequestResponse {
  id: number
  businessName: string
  address: string
  addressDetail: string
  contactName: string
  contactPhone: string
  consultationRequestedAt: string
  createdAt: string
}

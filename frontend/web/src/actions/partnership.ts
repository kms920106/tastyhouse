'use server'

import { partnershipRepository } from '@/domains/partnership/partnership.repository'

export async function createPartnershipRequest({
  businessName,
  address,
  addressDetail,
  contactName,
  contactPhone,
  consultationRequestedAt,
}: {
  businessName: string
  address: string
  addressDetail: string
  contactName: string
  contactPhone: string
  consultationRequestedAt: string
}) {
  return partnershipRepository.createPartnershipRequest({
    businessName,
    address,
    addressDetail,
    contactName,
    contactPhone,
    consultationRequestedAt,
  })
}

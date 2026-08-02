import 'server-only'

import { api } from '@/lib/api'
import { PartnershipRequestCreateRequest, PartnershipRequestResponse } from './partnership.dto'

const ENDPOINT = '/api/partnership-requests'

export const partnershipRepository = {
  // 광고 및 제휴 신청
  async createPartnershipRequest(request: PartnershipRequestCreateRequest) {
    return api.post<PartnershipRequestResponse>(`${ENDPOINT}/v1`, request)
  },
}

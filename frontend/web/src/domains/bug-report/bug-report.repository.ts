import 'server-only'

import { api } from '@/lib/api'
import { BugReportCreateRequest, BugReportResponse } from './bug-report.dto'

const ENDPOINT = '/api/bug-reports'

export const bugReportRepository = {
  // 버그 제보 등록
  async createBugReport(request: BugReportCreateRequest) {
    return api.post<BugReportResponse>(`${ENDPOINT}/v1`, request)
  },
}

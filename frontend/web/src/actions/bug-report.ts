'use server'

import { bugReportRepository } from '@/domains/bug-report/bug-report.repository'

export async function createBugReport({
  device,
  title,
  content,
  uploadedFileIds,
}: {
  device: string
  title: string
  content: string
  uploadedFileIds?: number[]
}) {
  return bugReportRepository.createBugReport({ device, title, content, uploadedFileIds })
}

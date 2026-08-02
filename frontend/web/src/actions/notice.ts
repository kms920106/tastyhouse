'use server'

import { noticeRepository } from '@/domains/notice/notice.repository'

export async function getNoticeList({ page, size }: { page: number; size: number }) {
  return noticeRepository.getNoticeList({ page, size })
}

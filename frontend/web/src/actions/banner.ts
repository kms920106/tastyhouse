'use server'

import { bannerRepository } from '@/domains/banner/banner.repository'

export async function getHomeBanners({ page, size }: { page: number; size: number }) {
  return bannerRepository.getHomeBanners({ page, size })
}

export async function getSidebarBanners({ page, size }: { page: number; size: number }) {
  return bannerRepository.getSidebarBanners({ page, size })
}

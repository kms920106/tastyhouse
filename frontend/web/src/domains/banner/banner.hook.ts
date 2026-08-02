'use client'

import { getSidebarBanners } from '@/actions/banner'
import { useQuery } from '@tanstack/react-query'

export const bannerQueryKeys = {
  sidebar: ['banners', 'sidebar'] as const,
}

export function useSidebarBanners() {
  const { data, isLoading } = useQuery({
    queryKey: bannerQueryKeys.sidebar,
    queryFn: () => getSidebarBanners({ page: 0, size: 10 }),
    staleTime: Infinity,
  })

  return {
    banners: data?.data ?? [],
    isLoading,
  }
}

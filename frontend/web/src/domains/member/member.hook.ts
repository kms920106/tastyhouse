'use client'

import {
  getMemberProfile,
  getMemberStats,
  getMyBookmarks,
  getMyProfile,
  getMyReviewCount,
  getMyReviews,
  getMyStats,
  updateMemberProfile,
} from '@/actions/member'
import { getMemberReviews } from '@/actions/review'
import type { Member, UpdateProfileRequest } from '@/domains/member'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

const BOOKMARK_PAGE_SIZE = 10

export const memberQueryKeys = {
  myProfile: ['member', 'profile'] as const,
  myReviewCount: ['member', 'review-count'] as const,
  myStats: ['member', 'me', 'stats'] as const,
  myReviews: ['mypage', 'reviews'] as const,
  myBookmarks: ['mypage', 'bookmarks'] as const,
  stats: (memberId: number) => ['member', memberId, 'stats'] as const,
  profile: (memberId: number) => ['member', memberId, 'profile', 'basic'] as const,
  reviews: (memberId: number) => ['member', memberId, 'reviews'] as const,
}

interface EnabledOptions {
  enabled?: boolean
}

export function useMyProfile({ enabled = true }: EnabledOptions = {}) {
  const { data, isLoading, isError } = useQuery({
    queryKey: memberQueryKeys.myProfile,
    queryFn: async () => {
      const response = await getMyProfile()
      if (response.error) throw new Error(response.error)
      return response
    },
    staleTime: Infinity,
    enabled,
  })

  const memberProfile: Member | null = data?.data ?? null

  return {
    memberProfile,
    isLoading,
    isError,
    isLoggedIn: memberProfile !== null,
  }
}

export function useMemberStats(memberId: number | undefined) {
  return useQuery({
    queryKey: memberQueryKeys.stats(memberId!),
    queryFn: async () => {
      const response = await getMemberStats(memberId!)
      return response.data ?? null
    },
    enabled: !!memberId,
  })
}

export function useMyStats({ enabled = true }: EnabledOptions = {}) {
  return useQuery({
    queryKey: memberQueryKeys.myStats,
    queryFn: async () => {
      const response = await getMyStats()
      if (response.error) throw new Error(response.error)
      return response.data ?? null
    },
    enabled,
  })
}

export function useMemberProfile(memberId: number) {
  return useQuery({
    queryKey: memberQueryKeys.profile(memberId),
    queryFn: async () => {
      const response = await getMemberProfile(memberId)
      return response.data ?? null
    },
    enabled: !!memberId,
  })
}

export function useMyReviewCount({ enabled = true }: EnabledOptions = {}) {
  const { data, isLoading } = useQuery({
    queryKey: memberQueryKeys.myReviewCount,
    queryFn: () => getMyReviewCount(),
    enabled,
  })

  return {
    reviewCount: data?.data?.reviewCount ?? 0,
    isLoading,
  }
}

export function useMemberReviews(memberId: number, page = 0, size = 9) {
  return useQuery({
    queryKey: memberQueryKeys.reviews(memberId),
    queryFn: async () => {
      const response = await getMemberReviews(memberId, page, size)
      return {
        reviews: response.data || [],
        hasMore: (response.pagination?.totalElements ?? 0) > size,
      }
    },
  })
}

export function useMyReviews(page = 0, size = 9) {
  return useQuery({
    queryKey: memberQueryKeys.myReviews,
    queryFn: async () => {
      const response = await getMyReviews(page, size)
      return {
        reviews: response.data || [],
        hasMore: (response.pagination?.totalElements ?? 0) > size,
      }
    },
  })
}

export function useMyBookmarks() {
  return useQuery({
    queryKey: memberQueryKeys.myBookmarks,
    queryFn: async () => {
      const response = await getMyBookmarks(0, BOOKMARK_PAGE_SIZE)
      return {
        bookmarks: response.data || [],
        hasMoreBookmarks: (response.pagination?.totalElements ?? 0) > BOOKMARK_PAGE_SIZE,
      }
    },
  })
}

export function useUpdateMemberProfile(memberId?: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: UpdateProfileRequest) => updateMemberProfile(data),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: memberQueryKeys.myProfile })
      if (memberId) {
        await queryClient.invalidateQueries({ queryKey: memberQueryKeys.profile(memberId) })
      }
    },
  })
}

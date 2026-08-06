'use client'

import {
  createMemberDeliveryAddress,
  getMemberDeliveryAddresses,
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
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type {
  Member,
  MemberDeliveryAddress,
  MemberDeliveryAddressCreateRequest,
  UpdateProfileRequest,
} from '@/domains/member'
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
  myDeliveryAddresses: ['member', 'me', 'delivery-addresses'] as const,
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

/** 내 배달 주소록을 조회합니다. */
export function useMyDeliveryAddresses({ enabled = true }: EnabledOptions = {}) {
  const { data, isLoading, isError } = useQuery({
    queryKey: memberQueryKeys.myDeliveryAddresses,
    queryFn: async () => {
      const response = await getMemberDeliveryAddresses()
      if (response.error) throw new Error(response.error)
      return response.data ?? []
    },
    enabled,
  })

  const deliveryAddresses: MemberDeliveryAddress[] = data ?? []

  return { deliveryAddresses, isLoading, isError }
}

interface UseCreateMyDeliveryAddressOptions {
  /** 생성된 배달 주소 id를 받아 선택 상태를 갱신하는 콜백 */
  onSuccess?: (deliveryAddressId: number) => void
  onError?: (message: string) => void
}

/**
 * 내 배달 주소를 등록합니다.
 *
 * 좌표(latitude/longitude)는 호출하는 쪽이 카카오 로컬 API로 변환해서 채워 넣습니다 —
 * 좌표가 없는 주소는 거리별 배달팁이 계산되지 않아 서버가 저장을 거부합니다.
 */
export function useCreateMyDeliveryAddress({
  onSuccess,
  onError,
}: UseCreateMyDeliveryAddressOptions = {}) {
  const queryClient = useQueryClient()

  const { mutate, isPending } = useMutation({
    mutationFn: (data: MemberDeliveryAddressCreateRequest) => createMemberDeliveryAddress(data),
    onSuccess: (response) => {
      if (response.error || response.data == null) {
        onError?.(response.message ?? response.error ?? COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        return
      }
      queryClient.invalidateQueries({ queryKey: memberQueryKeys.myDeliveryAddresses })
      onSuccess?.(response.data)
    },
    onError: () => {
      onError?.(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
    },
  })

  return { createDeliveryAddress: mutate, isCreating: isPending }
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

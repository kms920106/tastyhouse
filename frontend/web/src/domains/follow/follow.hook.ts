'use client'

import { followMember, getFollowerList, getIsFollowing, getPublicFollowerList, getPublicFollowingList, getFollowingList, removeFollower, searchMembersByNickname, unfollowMember } from '@/actions/follow'
import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { IsFollowingResponse } from '@/domains/follow/follow.dto'
import { InfiniteData, useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

const PAGE_SIZE = 10

export const followQueryKeys = {
  allFollowers: () => ['followers'] as const,
  allFollowing: () => ['following'] as const,
  allMemberSearch: () => ['memberSearch'] as const,
  followers: (memberId: number, isLoggedIn: boolean) => ['followers', memberId, isLoggedIn] as const,
  following: (memberId: number, isLoggedIn: boolean) => ['following', memberId, isLoggedIn] as const,
  isFollowing: (memberId: number) => ['member', memberId, 'is-following'] as const,
  memberSearch: (searchQuery: string) => ['memberSearch', searchQuery] as const,
}

export function useFollowers(memberId: number, isLoggedIn: boolean) {
  return useInfiniteQuery({
    queryKey: followQueryKeys.followers(memberId, isLoggedIn),
    queryFn: async ({ pageParam }) => {
      const response = isLoggedIn
        ? await getFollowerList(memberId, { page: pageParam as number, size: PAGE_SIZE })
        : await getPublicFollowerList(memberId, { page: pageParam as number, size: PAGE_SIZE })
      return response
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage.pagination) return undefined
      const { page, totalPages } = lastPage.pagination
      return page + 1 < totalPages ? page + 1 : undefined
    },
  })
}

export function useFollowing(memberId: number, isLoggedIn: boolean) {
  return useInfiniteQuery({
    queryKey: followQueryKeys.following(memberId, isLoggedIn),
    queryFn: async ({ pageParam }) => {
      const response = isLoggedIn
        ? await getFollowingList(memberId, { page: pageParam as number, size: PAGE_SIZE })
        : await getPublicFollowingList(memberId, { page: pageParam as number, size: PAGE_SIZE })
      return response
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage.pagination) return undefined
      const { page, totalPages } = lastPage.pagination
      return page + 1 < totalPages ? page + 1 : undefined
    },
  })
}

export function useIsFollowing(memberId: number | undefined, enabled = true) {
  return useQuery({
    queryKey: followQueryKeys.isFollowing(memberId!),
    queryFn: async () => {
      const response = await getIsFollowing(memberId!)
      return response.data ?? null
    },
    enabled: !!memberId && enabled,
  })
}

interface FollowableMember {
  memberId: number
  following: boolean
}

interface PageData<T> {
  data?: T[]
  pagination?: {
    page: number
    size: number
    totalElements: number
    totalPages: number
  }
}

function updateFollowingStateInCache<T extends FollowableMember>(
  cache: InfiniteData<PageData<T>> | undefined,
  targetMemberId: number,
  following: boolean,
): InfiniteData<PageData<T>> | undefined {
  if (!cache) return cache
  return {
    ...cache,
    pages: cache.pages.map((page) => ({
      ...page,
      data: page.data?.map((m) =>
        m.memberId === targetMemberId ? { ...m, following } : m,
      ),
    })),
  }
}

export function useFollowMutation() {
  const queryClient = useQueryClient()

  const syncFollowState = (targetMemberId: number, following: boolean) => {
    queryClient.setQueriesData<InfiniteData<PageData<FollowableMember>>>(
      { queryKey: followQueryKeys.allFollowing(), exact: false },
      (old) => updateFollowingStateInCache(old, targetMemberId, following),
    )

    queryClient.setQueriesData<InfiniteData<PageData<FollowableMember>>>(
      { queryKey: followQueryKeys.allFollowers(), exact: false },
      (old) => updateFollowingStateInCache(old, targetMemberId, following),
    )

    queryClient.setQueriesData<InfiniteData<PageData<FollowableMember>>>(
      { queryKey: followQueryKeys.allMemberSearch(), exact: false },
      (old) => updateFollowingStateInCache(old, targetMemberId, following),
    )

    queryClient.setQueryData<IsFollowingResponse>(
      followQueryKeys.isFollowing(targetMemberId),
      (old) => old ? { ...old, following } : old,
    )
  }

  const followMutation = useMutation({
    mutationFn: (targetMemberId: number) => followMember(targetMemberId),
    onSuccess: (_, targetMemberId) => {
      syncFollowState(targetMemberId, true)
    },
    onError: () => {
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
    },
  })

  const unfollowMutation = useMutation({
    mutationFn: (targetMemberId: number) => unfollowMember(targetMemberId),
    onSuccess: (_, targetMemberId) => {
      syncFollowState(targetMemberId, false)
    },
    onError: () => {
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
    },
  })

  const handleFollowToggle = (member: FollowableMember) => {
    if (member.following) {
      unfollowMutation.mutate(member.memberId)
    } else {
      followMutation.mutate(member.memberId)
    }
  }

  return { handleFollowToggle }
}

export function useRemoveFollower(memberId: number, isLoggedIn: boolean) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (targetMemberId: number) => removeFollower(targetMemberId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: followQueryKeys.followers(memberId, isLoggedIn) })
    },
    onError: () => {
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
    },
  })
}

export function useMemberSearch(searchQuery: string) {
  return useInfiniteQuery({
    queryKey: followQueryKeys.memberSearch(searchQuery),
    queryFn: async ({ pageParam }) => {
      return searchMembersByNickname(searchQuery, pageParam as number, PAGE_SIZE)
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage.pagination) return undefined
      const { page, totalPages } = lastPage.pagination
      return page + 1 < totalPages ? page + 1 : undefined
    },
    enabled: searchQuery.trim().length > 0,
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 10,
  })
}

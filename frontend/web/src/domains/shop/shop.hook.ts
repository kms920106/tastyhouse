'use client'

import {
  getBestShops,
  getLatestShops,
  getShopDetail,
  getShopFoodTypes,
  getShopInfo,
  getShopMenus,
  getShopPhotos,
  getShopReviewStatistics,
  getShopReviews,
  toggleShopBookmark,
} from '@/actions/shop'
import { toast } from '@/components/ui/AppToaster'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { ShopAmenityCode, ShopFoodType } from '@/domains/shop/shop.types'
import type { ApiResponse } from '@/lib/api'
import {
  InfiniteData,
  useInfiniteQuery,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { useState } from 'react'

const BEST_PAGE_SIZE = 10
const LATEST_PAGE_SIZE = 6
const REVIEWS_PAGE_SIZE = 5

interface LatestShopsFilter {
  stationId?: number
  foodTypes?: ShopFoodType[]
  amenities?: ShopAmenityCode[]
}

export const shopQueryKeys = {
  best: ['places', 'best'] as const,
  latest: (filter: LatestShopsFilter) => ['places', 'latest', filter] as const,
  foodTypes: ['place', 'food-types'] as const,
  infoDetail: (shopId: number) => ['place', shopId, 'place-detail-info'] as const,
  menus: (shopId: number) => ['place', shopId, 'place-detail-menus'] as const,
  photos: (shopId: number) => ['place', shopId, 'place-detail-photos'] as const,
  reviewStatistics: (shopId: number) => ['place', shopId, 'place-review-statistics'] as const,
  reviews: (shopId: number) => ['place', shopId, 'place-detail-reviews'] as const,
}

export function useBestShops() {
  return useInfiniteQuery({
    queryKey: shopQueryKeys.best,
    queryFn: async ({ pageParam }) => {
      const response = await getBestShops({ page: pageParam, size: BEST_PAGE_SIZE })
      if (response.error) throw new Error(response.error)
      if (!response.data) throw new Error('응답 데이터가 없습니다.')
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

export function useLatestShops(filter: LatestShopsFilter) {
  return useInfiniteQuery({
    queryKey: shopQueryKeys.latest(filter),
    queryFn: async ({ pageParam }) => {
      const response = await getLatestShops({
        page: pageParam,
        size: LATEST_PAGE_SIZE,
        ...filter,
      })

      if (response.error) {
        throw new Error(response.error)
      }

      if (!response.data) {
        throw new Error('응답 데이터가 없습니다.')
      }

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

export function useShopReviews(shopId: number) {
  return useQuery({
    queryKey: shopQueryKeys.reviews(shopId),
    queryFn: () => getShopReviews(shopId, { page: 0, size: REVIEWS_PAGE_SIZE }),
  })
}

export function useShopInfoDetail(shopId: number) {
  return useQuery({
    queryKey: shopQueryKeys.infoDetail(shopId),
    queryFn: async () => {
      const [infoRes, detailRes] = await Promise.all([
        getShopInfo(shopId),
        getShopDetail(shopId),
      ])
      return { infoRes, detailRes }
    },
  })
}

export function useShopMenus(shopId: number) {
  return useQuery({
    queryKey: shopQueryKeys.menus(shopId),
    queryFn: () => getShopMenus(shopId),
  })
}

export function useShopPhotos(shopId: number) {
  return useQuery({
    queryKey: shopQueryKeys.photos(shopId),
    queryFn: () => getShopPhotos(shopId),
  })
}

export function useShopReviewStatistics(shopId: number) {
  return useQuery({
    queryKey: shopQueryKeys.reviewStatistics(shopId),
    queryFn: () => getShopReviewStatistics(shopId),
  })
}

export function useShopFoodTypes() {
  const { data, isLoading } = useQuery({
    queryKey: shopQueryKeys.foodTypes,
    queryFn: () => getShopFoodTypes(),
    staleTime: Infinity,
  })

  return {
    foodTypes: data?.data ?? [],
    isLoading,
  }
}

interface UseShopBookmarkProps {
  shopId: number
  initialIsBookmarked: boolean
}

interface BookmarkableShop {
  shopId: number
  bookmarked: boolean
}

const SEARCH_PLACES_PREVIEW_KEY = ['search', 'places', 'preview'] as const
const SEARCH_PLACES_INFINITE_KEY = ['search', 'places', 'infinite'] as const
const MYPAGE_BOOKMARKS_KEY = ['mypage', 'bookmarks'] as const

function updateBookmarkInPreviewCache<T extends BookmarkableShop>(
  cache: ApiResponse<T[]> | undefined,
  shopId: number,
  bookmarked: boolean,
): ApiResponse<T[]> | undefined {
  if (!cache?.data) return cache
  return {
    ...cache,
    data: cache.data.map((item) => (item.shopId === shopId ? { ...item, bookmarked } : item)),
  }
}

function updateBookmarkInInfiniteCache<T extends BookmarkableShop>(
  cache: InfiniteData<ApiResponse<T[]>> | undefined,
  shopId: number,
  bookmarked: boolean,
): InfiniteData<ApiResponse<T[]>> | undefined {
  if (!cache) return cache
  return {
    ...cache,
    pages: cache.pages.map((page) => ({
      ...page,
      data: page.data?.map((item) => (item.shopId === shopId ? { ...item, bookmarked } : item)),
    })),
  }
}

function updateBookmarkInMypageCache<T extends BookmarkableShop>(
  cache: { bookmarks: T[]; hasMoreBookmarks: boolean } | undefined,
  shopId: number,
  bookmarked: boolean,
): { bookmarks: T[]; hasMoreBookmarks: boolean } | undefined {
  if (!cache) return cache
  return {
    ...cache,
    bookmarks: cache.bookmarks.map((item) =>
      item.shopId === shopId ? { ...item, bookmarked } : item,
    ),
  }
}

export function useShopBookmark({ shopId, initialIsBookmarked }: UseShopBookmarkProps) {
  const queryClient = useQueryClient()
  const [isBookmarked, setIsBookmarked] = useState(initialIsBookmarked)

  const syncBookmarkState = (bookmarked: boolean) => {
    queryClient.setQueriesData<ApiResponse<BookmarkableShop[]>>(
      { queryKey: SEARCH_PLACES_PREVIEW_KEY, exact: false },
      (old) => updateBookmarkInPreviewCache(old, shopId, bookmarked),
    )
    queryClient.setQueriesData<InfiniteData<ApiResponse<BookmarkableShop[]>>>(
      { queryKey: SEARCH_PLACES_INFINITE_KEY, exact: false },
      (old) => updateBookmarkInInfiniteCache(old, shopId, bookmarked),
    )
    queryClient.setQueryData<{ bookmarks: BookmarkableShop[]; hasMoreBookmarks: boolean }>(
      MYPAGE_BOOKMARKS_KEY,
      (old) => updateBookmarkInMypageCache(old, shopId, bookmarked),
    )
  }

  const { mutate, isPending } = useMutation({
    mutationFn: () => toggleShopBookmark(shopId),
    onSuccess: ({ data, error }) => {
      if (error || !data) {
        toast(error || COMMON_ERROR_MESSAGES.MUTATION_ERROR)
        return
      }
      setIsBookmarked(data.bookmarked)
      syncBookmarkState(data.bookmarked)
    },
    onError: () => {
      toast(COMMON_ERROR_MESSAGES.MUTATION_ERROR)
    },
  })

  const toggleBookmark = () => {
    if (isPending) return
    mutate()
  }

  return { isBookmarked, isPending, toggleBookmark }
}

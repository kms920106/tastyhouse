'use client'

import { getSearchMenus, getSearchShops, getSearchPublicShops, getSearchReviews } from '@/actions/search'
import { useInfiniteQuery, useQuery } from '@tanstack/react-query'

const SEARCH_MENU_PREVIEW_SIZE = 3
const SEARCH_REVIEW_PREVIEW_SIZE = 9
const SEARCH_PLACE_PREVIEW_SIZE = 3
const SEARCH_MENU_PAGE_SIZE = 10
const SEARCH_REVIEW_PAGE_SIZE = 9
const SEARCH_PLACE_PAGE_SIZE = 10

export const searchQueryKeys = {
  menusPreview: (query: string) => ['search', 'menus', 'preview', query] as const,
  reviewsPreview: (query: string) => ['search', 'reviews', 'preview', query] as const,
  placesPreview: (query: string, isLoggedIn: boolean) =>
    ['search', 'places', 'preview', query, isLoggedIn] as const,
  menusInfinite: (query: string) => ['search', 'menus', 'infinite', query] as const,
  reviewsInfinite: (query: string) => ['search', 'reviews', 'infinite', query] as const,
  placesInfinite: (query: string, isLoggedIn: boolean) =>
    ['search', 'places', 'infinite', query, isLoggedIn] as const,
}

export function useSearchMenusPreview(query: string) {
  return useQuery({
    queryKey: searchQueryKeys.menusPreview(query),
    queryFn: () => getSearchMenus({ query, page: 0, size: SEARCH_MENU_PREVIEW_SIZE }),
    enabled: query.trim().length > 0,
  })
}

export function useSearchReviewsPreview(query: string) {
  return useQuery({
    queryKey: searchQueryKeys.reviewsPreview(query),
    queryFn: () => getSearchReviews({ query, page: 0, size: SEARCH_REVIEW_PREVIEW_SIZE }),
    enabled: query.trim().length > 0,
  })
}

export function useSearchShopsPreview(query: string, isLoggedIn: boolean) {
  return useQuery({
    queryKey: searchQueryKeys.placesPreview(query, isLoggedIn),
    queryFn: () =>
      isLoggedIn
        ? getSearchShops({ query, page: 0, size: SEARCH_PLACE_PREVIEW_SIZE })
        : getSearchPublicShops({ query, page: 0, size: SEARCH_PLACE_PREVIEW_SIZE }),
    enabled: query.trim().length > 0,
  })
}

export function useSearchMenusInfinite(query: string) {
  return useInfiniteQuery({
    queryKey: searchQueryKeys.menusInfinite(query),
    queryFn: ({ pageParam }) =>
      getSearchMenus({ query, page: pageParam, size: SEARCH_MENU_PAGE_SIZE }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage.pagination) return undefined
      const { page, totalPages } = lastPage.pagination
      return page + 1 < totalPages ? page + 1 : undefined
    },
    enabled: query.trim().length > 0,
  })
}

export function useSearchReviewsInfinite(query: string) {
  return useInfiniteQuery({
    queryKey: searchQueryKeys.reviewsInfinite(query),
    queryFn: ({ pageParam }) =>
      getSearchReviews({ query, page: pageParam, size: SEARCH_REVIEW_PAGE_SIZE }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage.pagination) return undefined
      const { page, totalPages } = lastPage.pagination
      return page + 1 < totalPages ? page + 1 : undefined
    },
    enabled: query.trim().length > 0,
  })
}

export function useSearchShopsInfinite(query: string, isLoggedIn: boolean) {
  return useInfiniteQuery({
    queryKey: searchQueryKeys.placesInfinite(query, isLoggedIn),
    queryFn: ({ pageParam }) =>
      isLoggedIn
        ? getSearchShops({ query, page: pageParam, size: SEARCH_PLACE_PAGE_SIZE })
        : getSearchPublicShops({ query, page: pageParam, size: SEARCH_PLACE_PAGE_SIZE }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage.pagination) return undefined
      const { page, totalPages } = lastPage.pagination
      return page + 1 < totalPages ? page + 1 : undefined
    },
    enabled: query.trim().length > 0,
  })
}

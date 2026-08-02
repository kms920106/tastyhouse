'use client'

import { ShopBestListItem } from '@/components/shops/ShopBestListItem'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useBestShops } from '@/domains/shop/shop.hook'
import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import { useEffect } from 'react'
import BestShopListSkeleton from './BestShopListSkeleton'

export default function BestShopListContent() {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading, isError } =
    useBestShops()

  const { targetRef, isIntersecting, resetIntersecting } = useIntersectionObserver({
    threshold: 0.1,
    rootMargin: '100px',
    enabled: hasNextPage && !isFetchingNextPage,
  })

  useEffect(() => {
    if (isIntersecting && hasNextPage && !isFetchingNextPage) {
      resetIntersecting()
      fetchNextPage()
    }
  }, [isIntersecting, hasNextPage, isFetchingNextPage, fetchNextPage, resetIntersecting])

  const places = data?.pages.flatMap((page) => page.data ?? []) ?? []

  return (
    <div className="px-[15px] py-8">
      {isLoading ? (
        <BestShopListSkeleton />
      ) : isError ? (
        <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
      ) : (
        <>
          <ul className="grid grid-cols-2 gap-x-[15px] gap-y-10">
            {places.map((place) => (
              <ShopBestListItem
                key={place.id}
                id={place.id}
                name={place.name}
                imageUrl={place.imageUrl}
                stationName={place.stationName}
                rating={place.rating}
                foodTypes={place.foodTypes}
              />
            ))}
          </ul>
          {isFetchingNextPage && <BestShopListSkeleton count={2} />}
          <div ref={targetRef} className="h-1" aria-hidden="true" />
        </>
      )}
    </div>
  )
}

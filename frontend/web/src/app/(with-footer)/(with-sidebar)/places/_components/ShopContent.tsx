'use client'

import { ShopCardSkeleton } from '@/components/shops/ShopCardSkeleton'
import FetchErrorState from '@/components/ui/FetchErrorState'
import type { ShopAmenityCode, ShopFoodType } from '@/domains/shop'
import { useLatestShops } from '@/domains/shop/shop.hook'
import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useEffect } from 'react'
import ShopFilterBar from './ShopFilterBar'
import { ShopContentSkeleton } from './ShopContentSkeleton'
import ShopListItem from './ShopListItem'

function LoadingIndicator() {
  return (
    <>
      {[...Array(2)].map((_, i) => (
        <li key={`loading-${i}`}>
          <ShopCardSkeleton />
        </li>
      ))}
    </>
  )
}

interface Props {
  stationId?: number
  foodTypes?: ShopFoodType[]
  amenities?: ShopAmenityCode[]
}

export default function ShopContent({ stationId, foodTypes, amenities }: Props) {
  const filterParams = { stationId, foodTypes, amenities }

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading, isError } =
    useLatestShops(filterParams)

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

  if (isLoading) {
    return <ShopContentSkeleton />
  }

  if (isError) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  if (!data?.pages || data.pages.length === 0) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('플레이스')} />
  }

  const shops = data.pages.flatMap((page) => page.data ?? [])
  const totalCount = data.pages[0]?.pagination?.totalElements ?? 0

  return (
    <div className="min-h-screen px-[15px] py-[30px] pb-[90px]">
      <ShopFilterBar
        totalCount={totalCount}
        stationId={stationId}
        foodTypes={foodTypes}
        amenities={amenities}
      />
      <ul className="grid grid-cols-2 gap-x-[15px] gap-y-10">
        {shops.map((shop) => (
          <ShopListItem
            key={shop.id}
            id={shop.id}
            name={shop.name}
            imageUrl={shop.imageUrl}
            stationName={shop.stationName}
            rating={shop.rating}
            reviewCount={shop.reviewCount}
            bookmarkCount={shop.bookmarkCount}
            foodTypes={shop.foodTypes}
          />
        ))}
        {isFetchingNextPage && <LoadingIndicator />}
      </ul>
      <div ref={targetRef} className="h-1" aria-hidden="true" />
    </div>
  )
}

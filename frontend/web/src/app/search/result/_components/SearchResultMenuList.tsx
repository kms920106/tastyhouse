'use client'

import ProductItem from '@/components/products/ProductItem'
import { ProductItemSkeleton } from '@/components/products/ProductItemSkeleton'
import Divider from '@/components/ui/Divider'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useSearchMenusInfinite } from '@/domains/search/search.hook'
import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import React, { useEffect } from 'react'
import SearchResultEmptyState from './SearchResultEmptyState'

interface Props {
  query: string
}

export default function SearchResultMenuList({ query }: Props) {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading, isError } =
    useSearchMenusInfinite(query)

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

  if (isLoading)
    return (
      <div className="px-[15px] py-5">
        {Array.from({ length: 10 }).map((_, i) => (
          <React.Fragment key={i}>
            <ProductItemSkeleton />
            {i < 9 && <Divider />}
          </React.Fragment>
        ))}
      </div>
    )
  if (isError) return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />

  const totalElements = data?.pages[0]?.pagination?.totalElements ?? 0
  if (totalElements === 0) return <SearchResultEmptyState query={query} label="메뉴" />

  const products = data?.pages.flatMap((page) => page.data ?? []) ?? []

  return (
    <div className="px-[15px] py-5">
      {products.map((product, i, arr) => (
        <React.Fragment key={product.id}>
          <Link href={PAGE_PATHS.PRODUCT_DETAIL(product.id)} className="block">
            <ProductItem
              imageUrl={product.imageUrl}
              spiciness={product.spiciness}
              name={product.name}
              originalPrice={product.originalPrice}
              discountPrice={product.discountPrice}
              discountRate={product.discountRate}
              rating={product.rating}
              reviewCount={product.reviewCount}
            />
          </Link>
          {i < arr.length - 1 && <Divider />}
        </React.Fragment>
      ))}
      {isFetchingNextPage && (
        <>
          <Divider />
          <div>
            {Array.from({ length: 2 }).map((_, i) => (
              <React.Fragment key={i}>
                <ProductItemSkeleton />
                {i < 1 && <Divider />}
              </React.Fragment>
            ))}
          </div>
        </>
      )}
      <div ref={targetRef} className="h-1" aria-hidden="true" />
    </div>
  )
}

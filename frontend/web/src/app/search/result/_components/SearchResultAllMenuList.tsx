'use client'

import ProductItem from '@/components/products/ProductItem'
import { ProductItemSkeleton } from '@/components/products/ProductItemSkeleton'
import Divider from '@/components/ui/Divider'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useSearchMenusPreview } from '@/domains/search/search.hook'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import React from 'react'
import SearchResultEmptyState from './SearchResultEmptyState'

interface Props {
  query: string
}

export default function SearchResultAllMenuList({ query }: Props) {
  const { data, isLoading, isError } = useSearchMenusPreview(query)

  if (isLoading)
    return (
      <div>
        {Array.from({ length: 3 }).map((_, i) => (
          <React.Fragment key={i}>
            <ProductItemSkeleton />
            {i < 2 && <Divider />}
          </React.Fragment>
        ))}
      </div>
    )
  if (isError) return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('메뉴')} />
  if (!data?.data || data.data.length === 0)
    return <SearchResultEmptyState query={query} label="메뉴" />

  return (
    <div>
      {data.data!.map((product, i, arr) => (
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
    </div>
  )
}

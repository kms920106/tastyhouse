'use client'

import DiscountProductItem from '@/components/products/DiscountProductItem'
import Divider from '@/components/ui/Divider'
import FetchErrorState from '@/components/ui/FetchErrorState'
import Icon from '@/components/ui/Icon'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useTodayDiscountProducts } from '@/domains/product/product.hook'
import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import { parseAsStringLiteral, useQueryStates } from 'nuqs'
import { Fragment, useEffect, useMemo } from 'react'
import TodayDiscountGridItem from './TodayDiscountGridItem'
import TodayDiscountListSkeleton from './TodayDiscountListSkeleton'
import TodayDiscountSortDrawer, { type TodayDiscountSortType } from './TodayDiscountSortDrawer'

export type ViewType = 'list' | 'grid'

const VIEW_VALUES = ['list', 'grid'] as const satisfies readonly ViewType[]
const SORT_VALUES = [
  'RECOMMENDED',
  'DISCOUNT_RATE',
  'PRICE_LOW',
  'PRICE_HIGH',
] as const satisfies readonly TodayDiscountSortType[]

const urlStateParsers = {
  view: parseAsStringLiteral(VIEW_VALUES).withDefault('list'),
  sort: parseAsStringLiteral(SORT_VALUES).withDefault('RECOMMENDED'),
}

type Product = { discountRate: number; discountPrice: number }

const SORT_COMPARATORS: Record<TodayDiscountSortType, ((a: Product, b: Product) => number) | null> =
  {
    RECOMMENDED: null,
    DISCOUNT_RATE: (a, b) => b.discountRate - a.discountRate,
    PRICE_LOW: (a, b) => a.discountPrice - b.discountPrice,
    PRICE_HIGH: (a, b) => b.discountPrice - a.discountPrice,
  }

export default function TodayDiscountContent() {
  const [{ view, sort }, setParams] = useQueryStates(urlStateParsers)

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading, isError } =
    useTodayDiscountProducts()

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

  const products = useMemo(() => {
    const list = data?.pages.flatMap((page) => page.data ?? []) ?? []
    const comparator = SORT_COMPARATORS[sort]
    return comparator ? [...list].sort(comparator) : list
  }, [data, sort])

  return (
    <div className="px-[15px] pt-8">
      <div className="flex items-center justify-end gap-2.5">
        <TodayDiscountSortDrawer value={sort} onChange={(value) => setParams({ sort: value })} />
        <div className="w-px h-[15px] bg-[#cccccc]" aria-hidden="true" />
        <button
          type="button"
          aria-label={view === 'list' ? '그리드 뷰로 전환' : '리스트 뷰로 전환'}
          onClick={() => setParams({ view: view === 'list' ? 'grid' : 'list' })}
          className="flex items-center justify-center cursor-pointer"
        >
          <Icon name={view === 'list' ? 'today-discount/view-01' : 'today-discount/view-02'} />
        </button>
      </div>

      {isLoading ? (
        <TodayDiscountListSkeleton viewType={view} />
      ) : isError ? (
        <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
      ) : (
        <>
          {view === 'list' ? (
            <div className="py-5">
              {products.map((product, i, arr) => (
                <Fragment key={product.id}>
                  <Link href={PAGE_PATHS.PRODUCT_DETAIL(product.id)} className="block">
                    <DiscountProductItem {...product} />
                  </Link>
                  {i < arr.length - 1 && <Divider />}
                </Fragment>
              ))}
            </div>
          ) : (
            <div className="py-[25px]">
              <div className="grid grid-cols-2 gap-x-2.5 gap-y-5">
                {products.map((product) => (
                  <TodayDiscountGridItem key={product.id} {...product} />
                ))}
              </div>
            </div>
          )}
          {isFetchingNextPage && <TodayDiscountListSkeleton viewType={view} count={2} />}
          <div ref={targetRef} className="h-1" aria-hidden="true" />
        </>
      )}
    </div>
  )
}

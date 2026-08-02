'use client'

import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useProductOptions } from '@/domains/product/product.hook'
import ShopOrderMenuDetailOptionForm from './ShopOrderMenuDetailOptionForm'
import { ShopOrderMenuDetailOptionSelectorSkeleton } from './ShopOrderMenuDetailOptionSelectorSkeleton'

interface Props {
  productId: number
  shopId: number
}

export default function ShopOrderMenuDetailOptionTabContent({ productId, shopId }: Props) {
  const { data, isLoading, error } = useProductOptions(productId)

  if (isLoading) return <ShopOrderMenuDetailOptionSelectorSkeleton />
  if (error) return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  if (!data?.data)
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('옵션 정보')} />

  return (
    <ShopOrderMenuDetailOptionForm
      productId={productId}
      shopId={shopId}
      optionGroups={data.data.optionGroups}
    />
  )
}

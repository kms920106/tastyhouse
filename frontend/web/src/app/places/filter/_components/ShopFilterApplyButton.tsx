'use client'

import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import { useShopFilterState } from './ShopFilterStateProvider'

export default function ShopFilterApplyButton() {
  const { hasSelection, handleApplyFilter } = useShopFilterState()

  return (
    <AppPrimaryButton onClick={handleApplyFilter} disabled={!hasSelection}>
      필터 적용
    </AppPrimaryButton>
  )
}

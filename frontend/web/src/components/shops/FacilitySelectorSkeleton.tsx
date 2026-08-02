import { FacilityItemSkeleton } from '@/components/shops/FacilityItemSkeleton'
import { FacilitySelectorLayout } from '@/components/shops/FacilitySelectorLayout'

export function FacilitySelectorSkeleton() {
  return (
    <FacilitySelectorLayout>
      {Array.from({ length: 8 }).map((_, i) => (
        <FacilityItemSkeleton key={i} />
      ))}
    </FacilitySelectorLayout>
  )
}

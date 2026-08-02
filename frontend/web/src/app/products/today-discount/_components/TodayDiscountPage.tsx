import { Suspense } from 'react'
import TodayDiscountContent from './TodayDiscountContent'
import TodayDiscountHeader from './TodayDiscountHeader'
import TodayDiscountListSkeleton from './TodayDiscountListSkeleton'

/**
 * TodayDiscountContent는 nuqs의 useQueryStates(= useSearchParams)를 사용하므로 빌드 타임 프리렌더 시 CSR bail-out이 발생.
 * Suspense 경계가 없으면 next build가 실패하므로 제거하면 안됨.
 */

export default function TodayDiscountPage() {
  return (
    <>
      <TodayDiscountHeader />
      <Suspense fallback={<TodayDiscountListSkeleton viewType="list" />}>
        <TodayDiscountContent />
      </Suspense>
    </>
  )
}

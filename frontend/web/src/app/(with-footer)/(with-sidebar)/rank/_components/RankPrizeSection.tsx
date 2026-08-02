import { Suspense } from 'react'
import RankPrizeList from './RankPrizeList'
import { RankPrizeListSkeleton } from './RankPrizeListSkeleton'

export default function RankPrizeSection() {
  return (
    <section className="px-7 py-[30px] bg-white">
      <div className="flex justify-between items-end gap-2">
        <Suspense fallback={<RankPrizeListSkeleton />}>
          <RankPrizeList />
        </Suspense>
      </div>
    </section>
  )
}

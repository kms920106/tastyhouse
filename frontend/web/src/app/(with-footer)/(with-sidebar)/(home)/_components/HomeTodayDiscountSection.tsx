import { Suspense } from 'react'
import HomeSectionHeader from './HomeSectionHeader'
import HomeTodayDiscountProductList from './HomeTodayDiscountProductList'
import { HomeTodayDiscountProductListSkeleton } from './HomeTodayDiscountProductListSkeleton'

export default async function HomeTodayDiscountSection() {
  return (
    <section className="px-[15px] pt-[60px]">
      <HomeSectionHeader
        title="오늘의 할인"
        description="테하 고객만이 누릴 수 있는 할인 혜택을 놓치지 마세요."
      />
      <Suspense fallback={<HomeTodayDiscountProductListSkeleton />}>
        <HomeTodayDiscountProductList />
      </Suspense>
    </section>
  )
}

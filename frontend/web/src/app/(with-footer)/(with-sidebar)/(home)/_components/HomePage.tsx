import HomeBannerSection from './HomeBannerSection'
import HomeBestShopSection from './HomeBestShopSection'
import HomeBestReviewSection from './HomeBestReviewSection'
import HomeChoiceSection from './HomeChoiceSection'
import QuickReviewLink from '@/components/ui/QuickReviewLink'
import HomeTodayDiscountSection from './HomeTodayDiscountSection'
import HomeHeader from './HomeHeader'

export default function HomePage() {
  return (
    <>
      <HomeHeader />

      {/* 배너 */}
      <HomeBannerSection />

      {/* 베스트 리뷰 */}
      <HomeBestReviewSection />

      {/* 베스트 플레이스 */}
      <HomeBestShopSection />

      {/* 오늘의 할인 */}
      <HomeTodayDiscountSection />

      {/* 테하 초이스 */}
      <HomeChoiceSection />

      {/* 리뷰 작성 */}
      <div className="fixed bottom-18 left-0 right-0 z-[60] [body[data-scroll-locked]_&]:hidden">
        <QuickReviewLink />
      </div>

      <div className="h-[70px]" />
    </>
  )
}

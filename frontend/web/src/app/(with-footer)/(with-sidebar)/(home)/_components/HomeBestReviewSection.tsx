import HomeBestReviewContent from './HomeBestReviewContent'
import HomeSectionHeader from './HomeSectionHeader'

export default async function HomeBestReviewSection() {
  return (
    <section className="pt-[50px] pb-[60px]">
      <HomeSectionHeader
        title="베스트 리뷰"
        description="테하인들의 마음을 사로잡은 리뷰를 소개합니다."
      />
      <HomeBestReviewContent />
    </section>
  )
}

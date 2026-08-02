import HomeChoiceShopContent from './HomeChoiceShopContent'
import HomeSectionHeader from './HomeSectionHeader'

export default async function HomeChoiceSection() {
  return (
    <section className="pt-[60px]">
      <HomeSectionHeader
        title="테하 초이스"
        description="요즘 주목받고 있는 플레이스를 소개합니다."
      />
      <div className="pl-4 pb-10">
        <HomeChoiceShopContent />
      </div>
    </section>
  )
}

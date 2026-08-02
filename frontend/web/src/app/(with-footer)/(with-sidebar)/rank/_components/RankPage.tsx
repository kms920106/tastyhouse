import type { RankTab } from './RankMemberTabs'
import RankMyFixedSection from './RankMyFixedSection'
import RankPrizeSection from './RankPrizeSection'
import RankHeader from './RankHeader'
import RankMemberSection from './RankMemberSection'

interface Props {
  tab: RankTab
}

export default function RankPage({ tab }: Props) {
  return (
    <>
      <RankHeader />
      <div className="flex flex-col flex-1 gap-2.5 bg-[#f9f9f9]">
        <RankPrizeSection />
        <RankMemberSection tab={tab} />
        <RankMyFixedSection tab={tab} />
      </div>
    </>
  )
}

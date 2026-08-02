import MemberDetailHeader from './MemberDetailHeader'
import MemberDetailProfile from './MemberDetailProfile'
import type { MemberDetailTab } from './MemberDetailTabs'
import MemberDetailTabs from './MemberDetailTabs'

interface Props {
  memberId: number
  isLoggedIn: boolean
  tab: MemberDetailTab
}

export default function MemberDetailPage({ memberId, isLoggedIn, tab }: Props) {
  return (
    <div className="flex flex-col min-h-dvh bg-white">
      <div className="flex flex-col h-[50dvh]">
        <MemberDetailHeader memberId={memberId} isLoggedIn={isLoggedIn} />
        <MemberDetailProfile memberId={memberId} />
      </div>
      <MemberDetailTabs memberId={memberId} tab={tab} />
    </div>
  )
}

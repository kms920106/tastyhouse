import MemberFollowHeader from './MemberFollowHeader'
import MemberFollowTabs, { type MemberFollowTab } from './MemberFollowTabs'

interface Props {
  memberId: number
  tab: MemberFollowTab
  isLoggedIn: boolean
  isOwner: boolean
}

export default async function MemberFollowPage({ memberId, tab, isLoggedIn, isOwner }: Props) {
  return (
    <>
      <MemberFollowHeader memberId={memberId} />
      <MemberFollowTabs memberId={memberId} tab={tab} isLoggedIn={isLoggedIn} isOwner={isOwner} />
    </>
  )
}

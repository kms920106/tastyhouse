import { memberService } from '@/domains/member/member.service'
import { getIsLoggedIn } from '@/lib/auth-config'
import MemberFollowPage from './_components/MemberFollowPage'
import type { MemberFollowTab } from './_components/MemberFollowTabs'

interface Props {
  params: Promise<{
    id: string
  }>
  searchParams: Promise<{
    tab?: string
  }>
}

const MEMBER_FOLLOW_TAB_VALUES: MemberFollowTab[] = ['following', 'follower']

function parseMemberFollowTab(value: string | undefined): MemberFollowTab {
  return MEMBER_FOLLOW_TAB_VALUES.includes(value as MemberFollowTab)
    ? (value as MemberFollowTab)
    : 'follower'
}

export default async function Page({ params, searchParams }: Props) {
  const [{ id }, { tab }, isLoggedIn] = await Promise.all([params, searchParams, getIsLoggedIn()])
  const memberId = Number(id)
  const initialTab = parseMemberFollowTab(tab)

  let isOwner = false
  if (isLoggedIn) {
    const member = await memberService.getMyProfile()
    isOwner = member?.data?.id === memberId
  }

  return (
    <MemberFollowPage
      memberId={memberId}
      tab={initialTab}
      isLoggedIn={isLoggedIn}
      isOwner={isOwner}
    />
  )
}

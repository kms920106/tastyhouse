import { getIsLoggedIn } from '@/lib/auth-config'
import MemberDetailPage from './_components/MemberDetailPage'
import type { MemberDetailTab } from './_components/MemberDetailTabs'

interface Props {
  params: Promise<{ id: string }>
  searchParams: Promise<{ tab?: string }>
}

const MEMBER_DETAIL_TAB_VALUES: MemberDetailTab[] = ['reviews']

function parseMemberDetailTab(value: string | undefined): MemberDetailTab {
  return MEMBER_DETAIL_TAB_VALUES.includes(value as MemberDetailTab)
    ? (value as MemberDetailTab)
    : 'reviews'
}

export default async function Page({ params, searchParams }: Props) {
  const [{ id }, { tab }, isLoggedIn] = await Promise.all([params, searchParams, getIsLoggedIn()])
  const memberId = Number(id)

  return (
    <MemberDetailPage memberId={memberId} isLoggedIn={isLoggedIn} tab={parseMemberDetailTab(tab)} />
  )
}

import { getIsLoggedIn } from '@/lib/auth-config'
import MyPage from './_components/MyPage'
import type { MyPageTab } from './_components/MyPageTabs'

interface Props {
  searchParams: Promise<{
    tab?: string
  }>
}

const MY_PAGE_TAB_VALUES: MyPageTab[] = ['reviews', 'payments', 'bookmarks']

function parseMyPageTab(value: string | undefined): MyPageTab {
  return MY_PAGE_TAB_VALUES.includes(value as MyPageTab) ? (value as MyPageTab) : 'reviews'
}

export default async function Page({ searchParams }: Props) {
  const [isLoggedIn, { tab }] = await Promise.all([getIsLoggedIn(), searchParams])
  const initialTab = parseMyPageTab(tab)

  return <MyPage tab={initialTab} isLoggedIn={isLoggedIn} />
}

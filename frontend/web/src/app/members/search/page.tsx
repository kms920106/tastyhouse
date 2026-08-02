import { getIsLoggedIn } from '@/lib/auth-config'
import { PAGE_PATHS } from '@/lib/paths'
import { redirect } from 'next/navigation'
import MemberSearchPage from './_components/MemberSearchPage'

interface Props {
  searchParams: Promise<{
    q?: string
  }>
}

export default async function Page({ searchParams }: Props) {
  const [{ q }, isLoggedIn] = await Promise.all([searchParams, getIsLoggedIn()])

  if (!isLoggedIn) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  return <MemberSearchPage query={q ?? ''} />
}

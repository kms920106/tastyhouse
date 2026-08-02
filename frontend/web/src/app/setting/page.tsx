import { getIsLoggedIn } from '@/lib/auth-config'
import SettingPage from './_components/SettingPage'

export default async function Page() {
  const isLoggedIn = await getIsLoggedIn()

  return <SettingPage isLoggedIn={isLoggedIn} />
}
